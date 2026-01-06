# Comandos Git para Primer Commit

## Inicializar Repositorio (si no existe)

```bash
cd c:\Users\theca\Documents\GitHub\VolcanEngine
git init
```

## Configurar Usuario (si no está configurado)

```bash
git config user.name "Marvin-Dev"
git config user.email "marvin.dev@volcanengine.com"
```

## Agregar Todos los Archivos

```bash
git add .
```

## Crear Primer Commit

```bash
git commit -F PRIMER_COMMIT.txt
```

## Verificar Commit

```bash
git log --oneline
git show HEAD
```

## Crear Tag de Versión

```bash
git tag -a v1.0 -m "Volcan Engine v1.0 - Fundamentos Sólidos"
```

## Subir a Repositorio Remoto (si existe)

```bash
git remote add origin https://github.com/MarvinDev/VolcanEngine.git
git push -u origin main
git push --tags
```

---

**Nota**: El archivo PRIMER_COMMIT.txt contiene el mensaje completo del commit.
Usar `git commit -F PRIMER_COMMIT.txt` para incluir todo el mensaje.
