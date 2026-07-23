/* ElectraSync ERP — Minimal JS Utilities */

/* Auto-dismiss alerts after 4 seconds */
document.addEventListener('DOMContentLoaded', function () {
  document.querySelectorAll('.alert').forEach(function (el) {
    setTimeout(function () {
      el.style.transition = 'opacity 0.5s';
      el.style.opacity = '0';
      setTimeout(function () { el.remove(); }, 500);
    }, 4000);
  });

  /* Confirm-before-delete for any link with data-confirm attribute */
  document.querySelectorAll('[data-confirm]').forEach(function (el) {
    el.addEventListener('click', function (e) {
      if (!confirm(el.dataset.confirm)) e.preventDefault();
    });
  });
});
