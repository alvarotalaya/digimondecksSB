package net.ausiamarch.digimondecksSB.service;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

import net.ausiamarch.digimondecksSB.exception.ResourceNotFoundException;
import net.ausiamarch.digimondecksSB.exception.ResourceNotModifiedException;
import net.ausiamarch.digimondecksSB.exception.ValidationException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.gson.*;

import net.ausiamarch.digimondecksSB.entity.CardEntity;
import net.ausiamarch.digimondecksSB.helper.ValidationHelper;
import net.ausiamarch.digimondecksSB.repository.CardRepository;
import net.minidev.json.parser.ParseException;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Service
public class CardService {

    @Autowired
    CardRepository oCardRepository;

    @Autowired
    AuthService oAuthService;

    public void validate(Long id) {
        if (!oCardRepository.existsById(id)) {
            throw new ResourceNotFoundException("id " + id + " not exist");
        }
    }

    public void validate(CardEntity oCardEntity) {
        if (oCardRepository.existsByCardnumber(oCardEntity.getCardnumber())) {
            throw new ValidationException("this card already exists");
        }
    }

    public Long getAllCards() throws IOException, ParseException {
        oAuthService.OnlyAdmins();
        String[] colors = {"red", "blue", "yellow", "green", "black", "purple","white"};

        for(int i = 0; i < colors.length; i++){
            URL url = new URL("https://digimoncard.io/api-public/search.php?color=" + colors[i] + "&series=Digimon Card Game");
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            con.connect();

            int responsecode = con.getResponseCode();
            if (responsecode != 200) {
                throw new RuntimeException("HttpResponseCode: " + responsecode);
            } else {
              
                String inline = "";
                Scanner scanner = new Scanner(url.openStream());
              
               //Write all the JSON data into a string using a scanner
                while (scanner.hasNext()) {
                   inline += scanner.nextLine();
                }
                
                //Close the scanner
                scanner.close();
            
                //Using the JSON simple library parse the string into a json object
                JsonParser parser = new JsonParser();
                JsonElement tradeElement = parser.parse(inline);
                JsonArray cardarray = tradeElement.getAsJsonArray();
                
                for(int j = 0; j < cardarray.size(); j++){
                    JsonObject card = cardarray.get(j).getAsJsonObject();

                    CardEntity oCardEntity = new CardEntity();
                    
                    if(card.get("cardnumber").isJsonNull()){
                        oCardEntity.setCardnumber(null);
                    } else {
                        String cardnumber = card.get("cardnumber").getAsString();
                        oCardEntity.setCardnumber(cardnumber);
                    }

                    if (oCardRepository.existsByCardnumber(oCardEntity.getCardnumber())) {
                        break;
                    }

                    if(card.get("name").isJsonNull()){
                        oCardEntity.setName(null);
                    } else {
                        System.out.println(card.get("name"));                        String name = card.get("name").getAsString();
                        oCardEntity.setName(name);
                    }

                    if(card.get("type").isJsonNull()){
                        oCardEntity.setType(null);
                    } else {
                        String type = card.get("type").getAsString();
                        oCardEntity.setType(type);
                    }

                    if(card.get("color").isJsonNull()){
                        oCardEntity.setColor(null);
                    } else {
                        String color = card.get("color").getAsString();
                        oCardEntity.setColor(color);
                    }

                    if(card.get("stage").isJsonNull()){
                        oCardEntity.setStage(null);
                    } else {
                        String stage = card.get("stage").getAsString();
                        oCardEntity.setStage(stage);
                    }

                    if(card.get("digi_type").isJsonNull()){
                        oCardEntity.setDigitype(null);
                    } else {
                        String digitype = card.get("digi_type").getAsString();
                        oCardEntity.setDigitype(digitype);
                    }

                    if(card.get("attribute").isJsonNull()){
                        oCardEntity.setAttribute(null);
                    } else {
                        String attribute = card.get("attribute").getAsString();
                        oCardEntity.setAttribute(attribute);
                    }

                    if(card.get("level").isJsonNull()){
                        oCardEntity.setLevel(null);
                    } else {
                        String level = card.get("level").getAsString();
                        oCardEntity.setLevel(level);
                    }

                    if(card.get("play_cost").isJsonNull()){
                        oCardEntity.setPlaycost(null);
                    } else {
                        String playcost = card.get("play_cost").getAsString();
                        oCardEntity.setPlaycost(playcost);
                    }

                    if(card.get("evolution_cost").isJsonNull()){
                        oCardEntity.setEvolutioncost(null);
                    } else {
                        String evolutioncost = card.get("evolution_cost").getAsString();
                        oCardEntity.setEvolutioncost(evolutioncost);
                    }

                    if(card.get("dp").isJsonNull()){
                        oCardEntity.setDp(null);
                    } else {
                        String dp = card.get("dp").getAsString();
                        oCardEntity.setDp(dp);
                    }

                    if(card.get("maineffect").isJsonNull()){
                        oCardEntity.setMaineffect(null);
                    } else {
                        String maineffect = card.get("maineffect").getAsString();
                        oCardEntity.setMaineffect(maineffect);
                    }

                    if(card.get("soureeffect").isJsonNull()){
                        oCardEntity.setSourceeffect(null);
                    } else {
                        String sourceeffect = card.get("soureeffect").getAsString();
                        oCardEntity.setSourceeffect(sourceeffect);
                    }

                    if(card.get("image_url").isJsonNull()){
                        oCardEntity.setImage(null);
                    } else {
                        String image = card.get("image_url").getAsString();
                        oCardEntity.setImage(image);
                    }
                    
                    oCardRepository.save(oCardEntity);
                }   
            }   
        }
        return oCardRepository.count();
    }

    public CardEntity get(Long id) {
        oAuthService.OnlyAdmins();
        validate(id);
        return oCardRepository.getById(id);
    }

    public Page<CardEntity> getPage(Pageable oPageable, String strFilter) {
        oAuthService.OnlyAdmins();
        ValidationHelper.validateRPP(oPageable.getPageSize());
        if (strFilter == null || strFilter.length() == 0) {
                return oCardRepository.findAll(oPageable);
        } else {
            return oCardRepository.findByNameIgnoreCase(strFilter, oPageable);
        }
     }

    public Long count() {
        oAuthService.OnlyAdmins();
        return oCardRepository.count();
    }

    public Long update(CardEntity oCardEntity) {
        validate(oCardEntity.getId());
        oAuthService.OnlyAdmins();
        return oCardRepository.save(oCardEntity).getId();
    }

    public Long create(CardEntity oNewCardEntity) {
        oAuthService.OnlyAdmins();
        validate(oNewCardEntity);
        oNewCardEntity.setId(0L);

        return oCardRepository.save(oNewCardEntity).getId();
    }

    public Long delete(Long id) {
        oAuthService.OnlyAdmins();
        validate(id);
        oCardRepository.deleteById(id);
        if (oCardRepository.existsById(id)) {
            throw new ResourceNotModifiedException("can't remove register " + id);
        } else {
            return id;
        }
    }

}

