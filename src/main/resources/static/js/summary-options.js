document.addEventListener("DOMContentLoaded", () => {
    const form = document.querySelector(".js-summary-selection-form");
    if (!form) {
        return;
    }

    const checkboxes = Array.from(form.querySelectorAll(".js-optional-extra-checkbox"));
    const addButtons = Array.from(form.querySelectorAll(".js-optional-extra-toggle"));
    const quantityInputs = Array.from(form.querySelectorAll(".js-extra-quantity"));
    const priceNodes = Array.from(form.querySelectorAll(".js-boiler-total, .js-featured-total"));
    const totalExtrasNode = form.querySelector(".js-total-selected-extras");
    const breakdownContainer = form.querySelector(".js-optional-extras-breakdown");
    const optionalDivider = form.querySelector(".js-optional-extras-divider");

    const baseExtrasTotal = Number(totalExtrasNode?.dataset.baseExtrasTotal || 0);

    const formatPrice = (value) => String(Math.max(0, Math.round(value)));
    const getQuantityInput = (extraId) => quantityInputs.find((input) => input.dataset.extraId === extraId);
    const getQuantity = (extraId) => {
        const input = getQuantityInput(extraId);
        if (!input) {
            return 1;
        }

        const quantity = Math.max(1, Math.min(9, Number(input.value || 1)));
        input.value = String(quantity);
        return quantity;
    };

    const getSelectedExtras = () => checkboxes
        .filter((checkbox) => checkbox.checked)
        .map((checkbox) => {
            const quantity = checkbox.dataset.repeatable === "true" ? getQuantity(checkbox.value) : 1;
            const unitPrice = Number(checkbox.dataset.extraPrice || 0);
            return {
                id: checkbox.value,
                title: checkbox.dataset.extraTitle || "",
                price: unitPrice * quantity,
                quantity
            };
        });

    const syncCardState = (checkbox) => {
        const card = checkbox.closest(".js-optional-extra-card");
        const toggle = card?.querySelector(".js-optional-extra-toggle");
        if (!card || !toggle) {
            return;
        }

        card.classList.toggle("is-selected", checkbox.checked);
        toggle.textContent = checkbox.checked ? "Added" : "Add";
        toggle.dataset.selected = String(checkbox.checked);
    };

    const syncRepeatableHiddenInputs = () => {
        form.querySelectorAll(".js-repeatable-extra-hidden").forEach((input) => input.remove());

        getSelectedExtras()
            .filter((extra) => extra.quantity > 1)
            .forEach((extra) => {
                for (let index = 1; index < extra.quantity; index += 1) {
                    const input = document.createElement("input");
                    input.type = "hidden";
                    input.name = "selectedExtras";
                    input.value = extra.id;
                    input.className = "js-repeatable-extra-hidden";
                    form.appendChild(input);
                }
            });
    };

    const renderBreakdown = (selectedExtras) => {
        if (!breakdownContainer) {
            return;
        }

        breakdownContainer.innerHTML = "";

        selectedExtras.forEach((extra) => {
            const row = document.createElement("div");
            row.className = "summary-breakdown-row summary-breakdown-row-optional";
            const quantityLabel = extra.quantity > 1 ? ` x${extra.quantity}` : "";

            const copy = document.createElement("div");
            copy.className = "summary-breakdown-optional-copy";

            const title = document.createElement("span");
            title.textContent = `${extra.title}${quantityLabel}`;

            const removeButton = document.createElement("button");
            removeButton.type = "button";
            removeButton.className = "summary-remove-extra js-remove-extra";
            removeButton.dataset.extraId = extra.id;
            removeButton.textContent = "Remove";

            const price = document.createElement("strong");
            price.textContent = `£${formatPrice(extra.price)}`;

            copy.append(title, removeButton);
            row.append(copy, price);
            breakdownContainer.appendChild(row);
        });

        breakdownContainer.toggleAttribute("hidden", selectedExtras.length === 0);
        if (optionalDivider) {
            optionalDivider.toggleAttribute("hidden", selectedExtras.length === 0);
        }
    };

    const updatePrices = () => {
        const selectedExtras = getSelectedExtras();
        const optionalExtrasTotal = selectedExtras.reduce((sum, extra) => sum + extra.price, 0);

        priceNodes.forEach((node) => {
            const basePrice = Number(node.dataset.basePrice || 0);
            node.textContent = formatPrice(basePrice + optionalExtrasTotal);
        });

        if (totalExtrasNode) {
            totalExtrasNode.textContent = formatPrice(baseExtrasTotal + optionalExtrasTotal);
        }

        syncRepeatableHiddenInputs();
        renderBreakdown(selectedExtras);
    };

    checkboxes.forEach((checkbox) => {
        syncCardState(checkbox);
        checkbox.addEventListener("change", () => {
            syncCardState(checkbox);
            updatePrices();
        });
    });

    addButtons.forEach((button) => {
        button.addEventListener("click", () => {
            const extraId = button.dataset.extraId;
            const checkbox = checkboxes.find((item) => item.value === extraId);
            if (!checkbox || checkbox.checked) {
                return;
            }

            checkbox.checked = true;
            syncCardState(checkbox);
            updatePrices();
        });
    });

    quantityInputs.forEach((input) => {
        input.addEventListener("change", () => {
            const checkbox = checkboxes.find((item) => item.value === input.dataset.extraId);
            if (checkbox && !checkbox.checked) {
                checkbox.checked = true;
                syncCardState(checkbox);
            }
            updatePrices();
        });
    });

    if (breakdownContainer) {
        breakdownContainer.addEventListener("click", (event) => {
            const removeButton = event.target.closest(".js-remove-extra");
            if (!removeButton) {
                return;
            }

            const extraId = removeButton.dataset.extraId;
            const checkbox = checkboxes.find((item) => item.value === extraId);
            if (!checkbox) {
                return;
            }

            checkbox.checked = false;
            const quantityInput = getQuantityInput(extraId);
            if (quantityInput) {
                quantityInput.value = "1";
            }
            syncCardState(checkbox);
            updatePrices();
        });
    }

    updatePrices();
});
