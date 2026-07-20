// Polyfill for sockjs-client / stompjs which require a Node-style `global`
if (typeof window !== 'undefined' && typeof window.global === 'undefined') {
  window.global = window;
}
