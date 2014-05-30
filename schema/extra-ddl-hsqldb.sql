CREATE TRIGGER introduction_update_concurrency BEFORE UPDATE ON introduction
REFERENCING NEW ROW AS NEW OLD ROW AS OLD
FOR EACH ROW
BEGIN ATOMIC
    IF OLD.recordVersion <> NEW.recordVersion THEN
        SIGNAL SQLSTATE '45000';
    END IF;

    SET NEW.recordVersion = NEW.recordVersion + 1;
END;

;;;
