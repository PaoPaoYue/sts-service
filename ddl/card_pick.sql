CREATE TABLE card_pick (
    unique_id String,         -- SHA hash of the tuple (card classpath, card_id, upgraded)
    card_id String,           -- card_id defined in the mod
    card_rarity String,       -- the rarity of the card (common, uncommon, rare, special, basic, curse)
    card_type String,         -- the type of the card (attack, skill, power, status, curse)
    card_cost Int32,          -- the cost of the card
    num_in_deck UInt32,       -- the number of this card in the deck before pick
    upgraded UInt8,           -- whether the card was upgraded
    picked UInt8,             -- whether the card was picked
    level UInt8,              -- the level of the dungeon
    ascension UInt8,          -- the game ascension  level
    user_name String,         -- the user who encountered this card
    character_name String,    -- the character whom the user was playing
    region String,            -- the region of the user
    timestamp DateTime,       -- the time when the card was encountered
    extra String              -- extra information about the card in JSON format
) ENGINE = MergeTree()
PRIMARY KEY (unique_id)
ORDER BY (unique_id, timestamp);