-- Drop the old primary key constraint
ALTER TABLE wallet_addresses DROP CONSTRAINT wallet_addresses_pkey;
-- Add the new composite primary key
ALTER TABLE wallet_addresses ADD PRIMARY KEY (user_id, address);
