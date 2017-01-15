# --- !Ups
CREATE OR REPLACE FUNCTION update_updated_at_column() RETURNS TRIGGER
    LANGUAGE 'plpgsql' AS $$
  BEGIN NEW.updated_at = NOW();;
    RETURN NEW;;
  END;;
$$;;

CREATE SEQUENCE drivers_sequence;

CREATE TABLE drivers
(
  id BIGINT NOT NULL DEFAULT nextval('drivers_sequence') PRIMARY KEY,
  name VARCHAR(200) NOT NULL,
  vehicle VARCHAR(50) NOT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT now(),
  updated_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TRIGGER drivers_trigger_updated_at BEFORE UPDATE ON drivers
FOR EACH ROW EXECUTE PROCEDURE update_updated_at_column();

CREATE SEQUENCE drivers_location_sequence;

CREATE TABLE drivers_location
(
  id BIGINT NOT NULL DEFAULT nextval('drivers_location_sequence') PRIMARY KEY,
  driver_id BIGINT NOT NULL,
  location GEOMETRY(POINT, 4326) NOT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT now(),
  updated_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TRIGGER drivers_location_trigger_updated_at BEFORE UPDATE ON drivers_location
FOR EACH ROW EXECUTE PROCEDURE update_updated_at_column();

CREATE INDEX drivers_id_idx ON drivers(id);
CREATE INDEX drivers_location_id_idx ON drivers_location(id);

# --- !Downs
DROP INDEX drivers_id_idx;
DROP INDEX drivers_location_id_idx;
DROP TRIGGER drivers_location_trigger_updated_at;
DROP TABLE drivers_location;
DROP SEQUENCE drivers_location_sequence;
DROP TRIGGER drivers_trigger_updated_at;
DROP TABLE drivers;
DROP SEQUENCE drivers_sequence;
DROP FUNCTION update_updated_at_column();