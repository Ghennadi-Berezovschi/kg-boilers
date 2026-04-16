document.addEventListener("DOMContentLoaded", () => {
    const form = document.querySelector(".js-summary-selection-form");
    if (!form) {
        return;
    }

    const checkboxes = Array.from(form.querySelectorAll(".js-optional-extra-checkbox"));
    const addButtons = Array.from(form.querySelectorAll(".js-optional-extra-toggle"));
    const priceNodes = Array.from(form.querySelectorAll(".js-boiler-total, .js-featured-total"));
    const totalExtrasNode = form.querySelector(".js-total-selected-extras");
    const breakdownContainer = form.querySelector(".js-optional-extras-breakdown");
    const optionalDivider = form.querySelector(".js-optional-extras-divider");

    const baseExtrasTotal = Number(totalExtrasNode?.dataset.baseExtrasTotal || 0);

    const formatPrice = (value) => String(Math.max(0, Math.round(value)));

    const getSelectedExtras = () => checkboxes
        .filter((checkbox) => checkbox.checked)
        .map((checkbox) => ({
            id: checkbox.value,
            title: checkbox.dataset.extraTitle || "",
            price: Number(checkbox.dataset.extraPrice || 0)
        }));

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

    const renderBreakdown = (selectedExtras) => {
        if (!breakdownContainer) {
            return;
        }

        breakdownContainer.innerHTML = "";

        selectedExtras.forEach((extra) => {
            const row = document.createElement("div");
            row.className = "summary-breakdown-row summary-breakdown-row-optional";
            row.innerHTML = `
                <div class="summary-breakdown-optional-copy">
                    <span>${extra.title}</span>
                    <button type="button" class="summary-remove-extra js-remove-extra" data-extra-id="${extra.id}">Remove</button>
                </div>
                <strong>£${formatPrice(extra.price)}</strong>
            `;
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
            syncCardState(checkbox);
            updatePrices();
        });
    }

    updatePrices();
});
