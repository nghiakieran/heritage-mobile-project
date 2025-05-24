package hcmute.edu.vn.heritageproject.api;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import hcmute.edu.vn.heritageproject.api.models.Heritage;
import hcmute.edu.vn.heritageproject.api.models.HeritageResponse;

/**
 * Utility class to handle JSON parsing for heritage data
 */
public class JsonParser {
    
    /**
     * Parse a HeritageResponse from a JSON object
     */
    public static HeritageResponse parseHeritageResponse(JSONObject jsonObject) throws JSONException {
        HeritageResponse response = new HeritageResponse();
        
        response.setSuccess(jsonObject.optBoolean("success", false));
        response.setMessage(jsonObject.optString("message", ""));
        
        // Parse heritages array based on context - different endpoints return different structures
        if (jsonObject.has("heritages")) {
            // Standard response format
            JSONArray jsonHeritages = jsonObject.getJSONArray("heritages");
            List<Heritage> heritageList = new ArrayList<>();
            
            for (int i = 0; i < jsonHeritages.length(); i++) {
                JSONObject jsonHeritage = jsonHeritages.getJSONObject(i);
                Heritage heritage = parseHeritage(jsonHeritage);
                heritageList.add(heritage);
            }
            
            response.setHeritages(heritageList);
        } else if (jsonObject.has("heritage")) {
            // Single heritage response (for getHeritageById or getHeritageBySlug endpoints)
            JSONObject jsonHeritage = jsonObject.getJSONObject("heritage");
            Heritage heritage = parseHeritage(jsonHeritage);
            
            List<Heritage> heritageList = new ArrayList<>();
            heritageList.add(heritage);
            response.setHeritages(heritageList);
        } else if (jsonObject.has("heritageNames")) {
            // Heritage names response (for getAllHeritageNames endpoint)
            JSONArray jsonHeritageNames = jsonObject.getJSONArray("heritageNames");
            List<hcmute.edu.vn.heritageproject.api.models.HeritageNameResponse> heritageNamesList = new ArrayList<>();
            
            for (int i = 0; i < jsonHeritageNames.length(); i++) {
                JSONObject jsonHeritageName = jsonHeritageNames.getJSONObject(i);
                hcmute.edu.vn.heritageproject.api.models.HeritageNameResponse heritageName = new hcmute.edu.vn.heritageproject.api.models.HeritageNameResponse();
                heritageName.setId(jsonHeritageName.optString("_id", ""));
                heritageName.setName(jsonHeritageName.optString("name", ""));
                heritageNamesList.add(heritageName);
            }
            
            response.setHeritageNames(heritageNamesList);
        }
        
        // Parse pagination if present
        if (jsonObject.has("pagination")) {
            JSONObject paginationJson = jsonObject.getJSONObject("pagination");
            HeritageResponse.Pagination pagination = new HeritageResponse.Pagination();
            
            pagination.setTotalItems(paginationJson.optInt("totalItems", 0));
            pagination.setCurrentPage(paginationJson.optInt("currentPage", 1));
            pagination.setTotalPages(paginationJson.optInt("totalPages", 1));
            pagination.setItemsPerPage(paginationJson.optInt("itemsPerPage", 10));
            
            response.setPagination(pagination);
        }
        
        // For the explore endpoint (nearest heritage sites)
        if (jsonObject.has("totalItems")) {
            response.setTotalItems(jsonObject.optInt("totalItems", 0));
        }
        
        return response;
    }
    
    /**
     * Parse a Heritage object from a JSON object
     */
    private static Heritage parseHeritage(JSONObject jsonObject) throws JSONException {
        Heritage heritage = new Heritage();
        
        // Parse basic fields
        heritage.setId(jsonObject.optString("_id", jsonObject.optString("id", "")));
        heritage.setName(jsonObject.optString("name", ""));
        heritage.setNameSlug(jsonObject.optString("nameSlug", ""));
        heritage.setDescription(jsonObject.optString("description", ""));
        heritage.setLocation(jsonObject.optString("location", ""));
        heritage.setLocationSlug(jsonObject.optString("locationSlug", ""));
        heritage.setLocationNormalized(jsonObject.optString("locationNormalized", ""));
        heritage.setStatus(jsonObject.optString("status", "ACTIVE"));
        
        // Optional date fields
        heritage.setCreatedAt(jsonObject.optLong("createdAt", 0));
        heritage.setUpdatedAt(jsonObject.optLong("updatedAt", 0));
        
        // Parse images array
        if (jsonObject.has("images")) {
            JSONArray jsonImages = jsonObject.getJSONArray("images");
            List<String> imagesList = new ArrayList<>();
            
            for (int i = 0; i < jsonImages.length(); i++) {
                imagesList.add(jsonImages.getString(i));
            }
            
            heritage.setImages(imagesList);
        }
        
        // Parse coordinates if present
        if (jsonObject.has("coordinates")) {
            JSONObject coordJson = jsonObject.getJSONObject("coordinates");
            Heritage.Coordinates coordinates = new Heritage.Coordinates();
            
            coordinates.setLatitude(coordJson.optString("latitude", "0"));
            coordinates.setLongitude(coordJson.optString("longitude", "0"));
            
            heritage.setCoordinates(coordinates);
        }
        
        // Parse stats if present
        if (jsonObject.has("stats")) {
            JSONObject statsJson = jsonObject.getJSONObject("stats");
            Heritage.Stats stats = new Heritage.Stats();
            
            stats.setAverageRating(statsJson.optString("averageRating", "0"));
            stats.setTotalReviews(statsJson.optString("totalReviews", "0"));
            stats.setTotalVisits(statsJson.optString("totalVisits", "0"));
            stats.setTotalFavorites(statsJson.optString("totalFavorites", "0"));
            
            heritage.setStats(stats);
        }
        
        // Parse tags
        if (jsonObject.has("popularTags")) {
            JSONArray tagsArray = jsonObject.getJSONArray("popularTags");
            List<String> tagsList = new ArrayList<>();
            
            for (int i = 0; i < tagsArray.length(); i++) {
                tagsList.add(tagsArray.getString(i));
            }
            
            heritage.setPopularTags(tagsList);
        }
        
        if (jsonObject.has("tagsSlug")) {
            JSONArray tagsArray = jsonObject.getJSONArray("tagsSlug");
            List<String> tagsList = new ArrayList<>();
            
            for (int i = 0; i < tagsArray.length(); i++) {
                tagsList.add(tagsArray.getString(i));
            }
            
            heritage.setTagsSlug(tagsList);
        }
        
        // Parse knowledge test related fields
        heritage.setKnowledgeTestId(jsonObject.optString("knowledgeTestId", null));
        heritage.setLeaderboardId(jsonObject.optString("leaderboardId", null));
        
        if (jsonObject.has("knowledgeTestSummary")) {
            JSONObject summaryJson = jsonObject.getJSONObject("knowledgeTestSummary");
            Heritage.KnowledgeTestSummary summary = new Heritage.KnowledgeTestSummary();
            
            summary.setTitle(summaryJson.optString("title", ""));
            summary.setQuestionCount(summaryJson.optString("questionCount", "0"));
            summary.setDifficulty(summaryJson.optString("difficulty", "Medium"));
            
            heritage.setKnowledgeTestSummary(summary);
        }
        
        // Parse leaderboard summary if present
        if (jsonObject.has("leaderboardSummary")) {
            JSONObject lbJson = jsonObject.getJSONObject("leaderboardSummary");
            Heritage.LeaderboardSummary lbSummary = new Heritage.LeaderboardSummary();
            
            lbSummary.setTopScore(lbJson.optString("topScore", "0"));
            lbSummary.setTotalParticipants(lbJson.optString("totalParticipants", "0"));
            
            if (lbJson.has("topUsers")) {
                JSONArray usersArray = lbJson.getJSONArray("topUsers");
                List<Heritage.TopUser> usersList = new ArrayList<>();
                
                for (int i = 0; i < usersArray.length(); i++) {
                    JSONObject userJson = usersArray.getJSONObject(i);
                    Heritage.TopUser user = new Heritage.TopUser();
                    
                    user.setUserId(userJson.optString("userId", ""));
                    user.setUserName(userJson.optString("userName", ""));
                    user.setScore(userJson.optInt("score", 0));
                    
                    usersList.add(user);
                }
                
                lbSummary.setTopUsers(usersList);
            }
            
            heritage.setLeaderboardSummary(lbSummary);
        }
        
        // Parse additional info if present
        if (jsonObject.has("additionalInfo")) {
            JSONObject infoJson = jsonObject.getJSONObject("additionalInfo");
            Heritage.AdditionalInfo additionalInfo = new Heritage.AdditionalInfo();
            
            additionalInfo.setArchitectural(infoJson.optString("architectural", null));
            additionalInfo.setCulturalFestival(infoJson.optString("culturalFestival", null));
            
            if (infoJson.has("historicalEvents")) {
                JSONArray eventsArray = infoJson.getJSONArray("historicalEvents");
                List<Heritage.HistoricalEvent> eventsList = new ArrayList<>();
                
                for (int i = 0; i < eventsArray.length(); i++) {
                    JSONObject eventJson = eventsArray.getJSONObject(i);
                    Heritage.HistoricalEvent event = new Heritage.HistoricalEvent();
                    
                    event.setTitle(eventJson.optString("title", ""));
                    event.setDescription(eventJson.optString("description", ""));
                    
                    eventsList.add(event);
                }
                
                additionalInfo.setHistoricalEvents(eventsList);
            }
            
            heritage.setAdditionalInfo(additionalInfo);
        }
        
        // Parse role play IDs if present
        if (jsonObject.has("rolePlayIds")) {
            JSONArray idsArray = jsonObject.getJSONArray("rolePlayIds");
            List<String> idsList = new ArrayList<>();
            
            for (int i = 0; i < idsArray.length(); i++) {
                idsList.add(idsArray.getString(i));
            }
            
            heritage.setRolePlayIds(idsList);
        }
        
        // Parse distance field for explore/nearest endpoint
        if (jsonObject.has("distance")) {
            heritage.setDistance(jsonObject.optDouble("distance", 0.0));
        }
        
        return heritage;
    }
}
