package com.github.paopaoyue.metrics.data;

import com.github.paopaoyue.metrics.proto.MetricsProto;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;

public class CardPick {

    private String uniqueId;         // SHA hash of the tuple (card classpath, card_id, upgraded)
    private String cardId;           // card_id defined in the mod
    private String cardRarity;       // the rarity of the card
    private String cardType;         // the type of the card (attack, skill, power, status, curse)
    private int cardCost;            // the cost of the card
    private int numInDeck;           // the number of this card in the deck before pick
    private boolean upgraded;        // whether the card was upgraded
    private boolean picked;          // whether the card was picked
    private int level;               // the level of the dungeon
    private int ascension;           // the game ascension level
    private String userName;         // the user who encountered this card
    private String characterName;    // the character whom the user was playing
    private String region;           // the region of the user
    private LocalDateTime timestamp; // the time when the card was encountered
    private String extra;            // extra information about the card in JSON format

    public static String generateUniqueId(MetricsProto.CardIdentifier identifier) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-1");

            String input = identifier.getCardId() + identifier.getClasspath() + identifier.getUpgraded();
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));

            StringBuilder sha1Hex = new StringBuilder();
            for (byte b : hash) {
                sha1Hex.append(String.format("%02x", b));
            }

            return sha1Hex.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error: SHA-1 algorithm not found", e);
        }
    }

    public String getUniqueId() {
        return uniqueId;
    }

    public void setUniqueId(String uniqueId) {
        this.uniqueId = uniqueId;
    }

    public String getCardId() {
        return cardId;
    }

    public void setCardId(String cardId) {
        this.cardId = cardId;
    }

    public String getCardRarity() {
        return cardRarity;
    }

    public void setCardRarity(String cardRarity) {
        this.cardRarity = cardRarity;
    }

    public String getCardType() {
        return cardType;
    }

    public void setCardType(String cardType) {
        this.cardType = cardType;
    }

    public int getNumInDeck() {
        return numInDeck;
    }

    public void setNumInDeck(int numInDeck) {
        this.numInDeck = numInDeck;
    }

    public int getCardCost() {
        return cardCost;
    }

    public void setCardCost(int cardCost) {
        this.cardCost = cardCost;
    }

    public boolean isUpgraded() {
        return upgraded;
    }

    public void setUpgraded(boolean upgraded) {
        this.upgraded = upgraded;
    }

    public boolean isPicked() {
        return picked;
    }

    public void setPicked(boolean picked) {
        this.picked = picked;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getAscension() {
        return ascension;
    }

    public void setAscension(int ascension) {
        this.ascension = ascension;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getCharacterName() {
        return characterName;
    }

    public void setCharacterName(String characterName) {
        this.characterName = characterName;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getExtra() {
        return extra;
    }

    public void setExtra(String extra) {
        this.extra = extra;
    }

    @Override
    public String toString() {
        return "CardPick{" +
                "uniqueId='" + uniqueId + '\'' +
                ", cardId='" + cardId + '\'' +
                ", cardRarity='" + cardRarity + '\'' +
                ", cardType='" + cardType + '\'' +
                ", cardCost=" + cardCost +
                ", numInDeck=" + numInDeck +
                ", upgraded=" + upgraded +
                ", picked=" + picked +
                ", level=" + level +
                ", ascension=" + ascension +
                ", userName='" + userName + '\'' +
                ", characterName='" + characterName + '\'' +
                ", region='" + region + '\'' +
                ", timestamp=" + timestamp +
                ", extra='" + extra + '\'' +
                '}';
    }
}
