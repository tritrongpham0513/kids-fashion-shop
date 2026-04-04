/**
 * Nút ẩn/hiện mật khẩu thống nhất: button.pwd-toggle-btn[data-password-target="inputId"]
 */
(function () {
  var EYE =
    '<path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"></path><circle cx="12" cy="12" r="3"></circle>';
  var EYE_OFF =
    '<path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19m-6.72-1.07a3 3 0 1 1-4.24-4.24"></path><line x1="1" y1="1" x2="23" y2="23"></line>';

  function setIcon(svg, visible) {
    svg.innerHTML = visible ? EYE_OFF : EYE;
  }

  function bind(btn) {
    if (btn.dataset.pwdToggleBound === '1') return;
    var id = btn.getAttribute('data-password-target');
    var input = id && document.getElementById(id);
    var svg = btn.querySelector('svg');
    if (!input || !svg) return;
    btn.dataset.pwdToggleBound = '1';

    btn.addEventListener('click', function () {
      var isPassword = input.getAttribute('type') === 'password';
      input.setAttribute('type', isPassword ? 'text' : 'password');
      setIcon(svg, isPassword);
      btn.setAttribute('aria-label', isPassword ? 'Ẩn mật khẩu' : 'Hiện mật khẩu');
      btn.setAttribute('aria-pressed', isPassword ? 'true' : 'false');
    });
  }

  function init() {
    document.querySelectorAll('button.pwd-toggle-btn[data-password-target]').forEach(bind);
  }

  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', init);
  } else {
    init();
  }
})();
