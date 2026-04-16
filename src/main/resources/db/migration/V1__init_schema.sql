-- 1. Tabla de Usuarios
CREATE TABLE users (
                       id_user UUID PRIMARY KEY,
                       id_google VARCHAR(50),
                       username VARCHAR(50) NOT NULL,
                       avatar_url VARCHAR(255),
                       email VARCHAR(255) UNIQUE NOT NULL,
                       role VARCHAR(50) NOT NULL
);

-- 2. Tabla de Especies
CREATE TABLE species (
                         id_species UUID PRIMARY KEY,
                         name VARCHAR(100) NOT NULL,
                         min_temperature NUMERIC(5,2),
                         max_temperature NUMERIC(5,2),
                         min_humidity NUMERIC(5,2),
                         max_humidity NUMERIC(5,2),
                         min_co2 NUMERIC(7,2),
                         max_co2 NUMERIC(7,2)
);

-- 3. Tabla de Sustratos
CREATE TABLE substrates (
                            id_substrate UUID PRIMARY KEY,
                            type_name VARCHAR(100) NOT NULL,
                            description VARCHAR(255)
);

-- 4. Tabla de Proveedores
CREATE TABLE suppliers (
                           id_supplier UUID PRIMARY KEY,
                           name_supplier VARCHAR(100) NOT NULL,
                           contact_info VARCHAR(255)
);

-- 5. Tabla de Lotes de Cultivo
CREATE TABLE crop_batch (
                            id_crop_batch UUID PRIMARY KEY,
                            id_species UUID REFERENCES species(id_species),
                            id_substrate UUID REFERENCES substrates(id_substrate),
                            id_species_supplier UUID REFERENCES suppliers(id_supplier),
                            id_substrate_supplier UUID REFERENCES suppliers(id_supplier),
                            id_user UUID REFERENCES users(id_user),
                            start_date TIMESTAMP NOT NULL,
                            end_date TIMESTAMP,
                            status VARCHAR(50) NOT NULL,
                            yield_kg NUMERIC(7,2)
);

-- 6. Tabla de Telemetría (El BIGSERIAL genera el BIGINT automáticamente)
CREATE TABLE telemetry_reading (
                                   id_telemetry BIGSERIAL PRIMARY KEY,
                                   id_crop_batch UUID REFERENCES crop_batch(id_crop_batch),
                                   recorded_at TIMESTAMP NOT NULL,
                                   temperature NUMERIC(5,2),
                                   humidity NUMERIC(5,2),
                                   co2 NUMERIC(7,2)
);