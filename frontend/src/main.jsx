import './polyfills.js';
import { StrictMode } from 'react';
import { createRoot } from 'react-dom/client';
import './index.css';
import App from './App.jsx';

// Interactive Touch/Click Sparkles Effect
window.addEventListener('click', (e) => {
  let canvas = document.getElementById('sparkle-canvas');
  if (!canvas) {
    canvas = document.createElement('div');
    canvas.id = 'sparkle-canvas';
    canvas.className = 'sparkle-canvas';
    document.body.appendChild(canvas);
  }
  const colors = ['#8b5cf6', '#a78bfa', '#ec4899', '#f43f5e', '#10b981', '#fbbf24'];
  for (let i = 0; i < 8; i++) {
    const p = document.createElement('div');
    p.className = 'sparkle-particle';
    const size = Math.random() * 6 + 4; // 4px to 10px
    const color = colors[Math.floor(Math.random() * colors.length)];
    
    p.style.width = `${size}px`;
    p.style.height = `${size}px`;
    p.style.background = color;
    p.style.boxShadow = `0 0 10px ${color}`;
    p.style.left = `${e.clientX}px`;
    p.style.top = `${e.clientY}px`;
    
    const angle = Math.random() * Math.PI * 2;
    const distance = Math.random() * 60 + 35; // 35px to 95px
    const tx = Math.cos(angle) * distance;
    const ty = Math.sin(angle) * distance;
    
    p.style.setProperty('--tx', `${tx}px`);
    p.style.setProperty('--ty', `${ty}px`);
    
    canvas.appendChild(p);
    
    setTimeout(() => {
      p.remove();
    }, 800);
  }
});

// Reusable Golden Spark Explosion Celebration
window.triggerGoldenSpark = (x, y) => {
  const clientX = x !== undefined ? x : window.innerWidth / 2;
  const clientY = y !== undefined ? y : window.innerHeight / 2;

  let canvas = document.getElementById('sparkle-canvas');
  if (!canvas) {
    canvas = document.createElement('div');
    canvas.id = 'sparkle-canvas';
    canvas.className = 'sparkle-canvas';
    document.body.appendChild(canvas);
  }

  for (let i = 0; i < 28; i++) {
    const p = document.createElement('div');
    p.className = 'sparkle-particle';
    const size = Math.random() * 10 + 6; // 6px to 16px (larger)
    const color = 'radial-gradient(circle, #ffe066 0%, #f59e0b 100%)';

    p.style.width = `${size}px`;
    p.style.height = `${size}px`;
    p.style.background = color;
    p.style.boxShadow = `0 0 15px #f59e0b, 0 0 30px #fbbf24`;
    p.style.left = `${clientX}px`;
    p.style.top = `${clientY}px`;

    const angle = Math.random() * Math.PI * 2;
    const distance = Math.random() * 140 + 40; // 40px to 180px explosion radius
    const tx = Math.cos(angle) * distance;
    const ty = Math.sin(angle) * distance;

    p.style.setProperty('--tx', `${tx}px`);
    p.style.setProperty('--ty', `${ty}px`);

    canvas.appendChild(p);

    setTimeout(() => {
      p.remove();
    }, 800);
  }
};

createRoot(document.getElementById('root')).render(
  <StrictMode>
    <App />
  </StrictMode>,
);
