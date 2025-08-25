/** @type {import('tailwindcss').Config} */
module.exports = {
  content: [
    "./src/main/resources/templates/**/*.html",
    "./src/main/resources/static/js/**/*.js"
  ],
  theme: {
    extend: {
      colors: {
        // Paleta base: brand + neutrales de Tailwind (slate/white)
        brand: '#065f46' // Primario/CTAs
      }
    }
  },
  plugins: []
};
