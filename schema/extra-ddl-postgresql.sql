CREATE OR REPLACE FUNCTION introduction_update_concurrency() RETURNS TRIGGER AS $$
BEGIN
    IF OLD.recordVersion <> NEW.recordVersion THEN
        RAISE EXCEPTION 'recordVersion does not match';
    END IF;

    NEW.recordVersion := NEW.recordVersion + 1;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql

;;;

CREATE TRIGGER introduction_update_concurrency BEFORE UPDATE ON introduction
FOR EACH ROW
EXECUTE PROCEDURE introduction_update_concurrency()

;;;
