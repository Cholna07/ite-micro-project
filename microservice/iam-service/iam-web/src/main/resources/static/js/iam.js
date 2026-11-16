document.addEventListener('click', (event) => {
  const button = event.target.closest('[data-password-toggle]');
  if (!button) return;

  const input = button.parentElement.querySelector('input');
  if (!input) return;

  const visible = input.type === 'password';
  input.type = visible ? 'text' : 'password';
  button.setAttribute('aria-pressed', String(visible));
  button.setAttribute('aria-label', visible ? button.dataset.hideLabel : button.dataset.showLabel);
});
