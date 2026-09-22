// Carga cards educativas desde API JSON y las renderiza en la landing.
// Ejecuta on-load, detecta idioma actual y fetcha landing-cards-{lang}.json via /api/landing-cards
async function loadLandingCards() {
  const container = document.querySelector('.landing-cards');
  if (!container) return; // No estamos en la landing

  try {
    const lang = document.documentElement.lang || 'es';
    const response = await fetch(`/api/landing-cards?lang=${lang}`);
    if (!response.ok) throw new Error(`HTTP ${response.status}`);

    const data = await response.json();
    container.innerHTML = ''; // Limpia placeholders

    data.cards.forEach(card => {
      const cardEl = document.createElement('a');
      cardEl.href = card.href;
      cardEl.className = 'landing-card';
      cardEl.innerHTML = `
        <div class="landing-card__icon">
          <svg class="icon"><use href="/images/icons.svg#${card.icon}"/></svg>
        </div>
        <h3 class="landing-card__title">${card.title}</h3>
        <p class="landing-card__desc">${card.description}</p>
        <button class="btn btn--outline" type="button">${card.button}</button>
      `;
      container.appendChild(cardEl);
    });
  } catch (error) {
    console.error('Error cargando landing cards:', error);
  }
}

// Ejecuta cuando el DOM esté listo
if (document.readyState === 'loading') {
  document.addEventListener('DOMContentLoaded', loadLandingCards);
} else {
  loadLandingCards();
}
