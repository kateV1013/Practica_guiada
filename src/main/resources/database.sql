CREATE DATABASE tienda_javafx;

-- Ejecutar el resto del script conectado a la base de datos tienda_javafx.

CREATE TABLE IF NOT EXISTS categoria (
    id SERIAL PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    activa BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE UNIQUE INDEX IF NOT EXISTS ux_categoria_nombre_lower
ON categoria (LOWER(nombre));

CREATE TABLE IF NOT EXISTS producto (
    id SERIAL PRIMARY KEY,
    codigo VARCHAR(50) NOT NULL UNIQUE,
    nombre VARCHAR(150) NOT NULL,
    categoria_id INTEGER NOT NULL,
    precio_venta NUMERIC(12,2) NOT NULL,
    existencia INTEGER NOT NULL DEFAULT 0,
    foto_ruta VARCHAR(500),
    activo BOOLEAN NOT NULL DEFAULT TRUE,

    CONSTRAINT fk_producto_categoria
        FOREIGN KEY (categoria_id)
        REFERENCES categoria(id)
);

ALTER TABLE producto
ADD COLUMN IF NOT EXISTS foto_ruta VARCHAR(500);
