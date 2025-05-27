/*
 *   ISEL-DEETC-SisInf
 *   ND 2022-2025
 *
 *   
 *   Information Systems Project - Active Databases
 *   
 */

/* ### DO NOT REMOVE THE QUESTION MARKERS ### */


-- region Question 1.a 
CREATE OR REPLACE TRIGGER ...
--TODO
-- endregion

-- region Question 1.b
CREATE OR REPLACE TRIGGER ...
--TODO
-- endregion

-- region Question 2
CREATE OR REPLACE FUNCTION fx_dock_occupancy(stationkid integer)
RETURNS NUMERIC AS $$
DECLARE
    total_docks INTEGER;
    occupied_docks INTEGER;
    occupancy_rate NUMERIC;
BEGIN
    -- Contar o número total de docas disponíveis (excluindo as em manutenção)
    SELECT COUNT(*) INTO total_docks
    FROM DOCK
    WHERE station = stationid
    AND state != 'under maintenance';
    
    -- Se não houver docas disponíveis, retornar 0
    IF total_docks = 0 THEN
        RETURN 0;
    END IF;
    
    -- Contar o número de docas ocupadas
    SELECT COUNT(*) INTO occupied_docks
    FROM DOCK
    WHERE station = stationid
    AND state = 'occupy';
    
    -- Calcular a taxa de ocupação (entre 0 e 1)
    occupancy_rate := occupied_docks::NUMERIC / total_docks::NUMERIC;
    
    RETURN occupancy_rate;
END;
$$ LANGUAGE plpgsql;
-- endregion
 
-- region Question 3
CREATE OR REPLACE FUNCTION insert_rider()
RETURNS TRIGGER AS $$
DECLARE
    person_id INTEGER;
    card_id INTEGER;
BEGIN
    -- Inserir na tabela PERSON
    INSERT INTO PERSON (email, taxnumber, name)
    VALUES (NEW.email, NEW.taxnumber, NEW.name)
    RETURNING id INTO person_id;
    
    -- Inserir na tabela CLIENT
    INSERT INTO CLIENT (person, dtregister)
    VALUES (person_id, COALESCE(NEW.dtregister, CURRENT_TIMESTAMP));
    
    -- Inserir na tabela CARD
    INSERT INTO CARD (credit, typeofcard, client)
    VALUES (NEW.credit, NEW.typeofcard, person_id)
    RETURNING id INTO card_id;
    
    -- Retornar os valores para a vista
    NEW.id := person_id;
    NEW.cardid := card_id;
    
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE TRIGGER trg_insert_rider
INSTEAD OF INSERT ON RIDER
FOR EACH ROW
EXECUTE FUNCTION insert_rider();

CREATE OR REPLACE FUNCTION update_rider()
RETURNS TRIGGER AS $$
BEGIN
    -- Atualizar a tabela PERSON
    UPDATE PERSON
    SET name = NEW.name,
        email = NEW.email
    WHERE id = NEW.id;
    
    -- Atualizar a tabela CARD
    IF NEW.credit IS NOT NULL THEN
        UPDATE CARD
        SET credit = NEW.credit,
            typeofcard = NEW.typeofcard
        WHERE id = NEW.cardid;
    END IF;
    
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE TRIGGER trg_update_rider
INSTEAD OF UPDATE ON RIDER
FOR EACH ROW
EXECUTE FUNCTION update_rider();

-- Recriar a vista RIDER para garantir que está atualizada
CREATE OR REPLACE VIEW RIDER
AS
SELECT p.*,c.dtregister,cd.id AS cardid,cd.credit,cd.typeofcard
FROM CLIENT c INNER JOIN PERSON p ON (c.person=p.id)
	INNER JOIN CARD cd ON (cd.client = c.person);
-- endregion

-- region Question 4
CREATE OR REPLACE PROCEDURE startTrip(dockid integer, clientid  integer) ...
--TODO
-- endregion