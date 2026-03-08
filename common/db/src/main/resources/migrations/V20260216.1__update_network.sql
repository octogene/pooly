ALTER TABLE prizes
ALTER
COLUMN network TYPE INTEGER
        USING CASE
                  WHEN network = 'BASE' THEN 0
                  WHEN network = 'OPTIMISM' THEN 1
                  WHEN network = 'ARBITRUM' THEN 2
                  WHEN network = 'SCROLL' THEN 3
                  WHEN network = 'GNOSIS' THEN 4
                  WHEN network = 'WORLD' THEN 5
END;

ALTER TABLE vaults
ALTER
COLUMN chain_network TYPE INTEGER
        USING CASE
                          WHEN network = 'BASE' THEN 0
                          WHEN network = 'OPTIMISM' THEN 1
                          WHEN network = 'ARBITRUM' THEN 2
                          WHEN network = 'SCROLL' THEN 3
                          WHEN network = 'GNOSIS' THEN 4
                          WHEN network = 'WORLD' THEN 5
END;