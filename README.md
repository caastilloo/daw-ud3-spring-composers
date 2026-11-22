# My Favourite Composers (DAW - UD3)

Aplicacion Spring Boot de Desarrollo Web en Entorno Servidor (unidad 4) preparada para desplegarse en la nube con Docker.

- `Dockerfile` multi-etapa: compila con Maven y ejecuta el fat-jar con un JRE 21.
- Perfil `docker`: escucha en `${PORT:8080}` y guarda los .csv en `data/csv` (se copian del jar al arrancar).
