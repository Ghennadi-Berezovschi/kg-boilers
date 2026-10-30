(() => {
    const gallery = document.querySelector('.about-work');
    const dialog = gallery?.querySelector('.work-lightbox');
    if (!dialog || typeof dialog.showModal !== 'function') return;

    const image = dialog.querySelector('img');
    const caption = dialog.querySelector('p');
    gallery.querySelectorAll('.work-photo').forEach(link => {
        link.addEventListener('click', event => {
            if (event.ctrlKey || event.metaKey || event.shiftKey || event.altKey) return;
            event.preventDefault();
            image.src = link.href;
            image.alt = link.querySelector('img').alt;
            caption.textContent = link.closest('figure').querySelector('h4').textContent;
            dialog.showModal();
        });
    });
    dialog.querySelector('button').addEventListener('click', () => dialog.close());
    dialog.addEventListener('click', event => {
        if (event.target !== dialog) return;
        const bounds = dialog.getBoundingClientRect();
        if (event.clientX < bounds.left || event.clientX > bounds.right ||
            event.clientY < bounds.top || event.clientY > bounds.bottom) dialog.close();
    });
})();
