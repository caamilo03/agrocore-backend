CREATE TABLE crop_batch (
                            id UUID PRIMARY KEY,
                            substrate_origin VARCHAR(255) NOT NULL,
                            start_date TIMESTAMP NOT NULL,
                            end_date TIMESTAMP,
                            status VARCHAR(50) NOT NULL
);