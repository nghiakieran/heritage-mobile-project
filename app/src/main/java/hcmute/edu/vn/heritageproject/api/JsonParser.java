package hcmute.edu.vn.heritageproject.api;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import hcmute.edu.vn.heritageproject.api.models.Heritage;
import hcmute.edu.vn.heritageproject.api.models.HeritageResponse;

public class JsonParser {
    private static final String TAG = "JsonParser";

    public static HeritageResponse parseHeritageResponse(JSONObject jsonObject) throws JSONException {
        HeritageResponse response = new HeritageResponse();

        response.setSuccess(jsonObject.optBoolean("success", false));
        response.setMessage(jsonObject.optString("message", ""));

        // Log JSON để debug
        Log.d(TAG, "Parsing JSON: " + jsonObject.toString());

        // Parse heritages array based on context
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

            response.setHeritageNames(heritageNamesList);        } else {
            // Trường hợp JSON không có "heritages", thử parse trực tiếp
            Log.w(TAG, "No 'heritages' field found, attempting to parse directly...");
            try {
                // Kiểm tra xem JSON có phải là đối tượng di tích không
                if (jsonObject.has("_id") && jsonObject.has("name")) {
                    Heritage heritage = parseHeritage(jsonObject);
                    List<Heritage> heritageList = new ArrayList<>();
                    heritageList.add(heritage);
                    response.setHeritages(heritageList);
                    response.setSuccess(true);
                    Log.d(TAG, "Successfully parsed single heritage object directly");
                } else {
                    throw new JSONException("JSON does not contain expected heritage fields");
                }
            } catch (Exception e) {
                Log.e(TAG, "Failed to parse directly: " + e.getMessage());
                response.setHeritages(new ArrayList<>());
                response.setMessage("Không tìm thấy dữ liệu di tích trong phản hồi.");
            }
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
    }    private static Heritage parseHeritage(JSONObject jsonObject) throws JSONException {
        Heritage heritage = new Heritage();

        // Xử lý ID phù hợp với đặc điểm API
        String id = jsonObject.optString("_id", "");
        if (id.isEmpty()) {
            id = jsonObject.optString("id", "");
        }
        Log.d(TAG, "Parsed heritage ID: " + id);
        heritage.setId(id);
        
        heritage.setName(jsonObject.optString("name", ""));
        heritage.setNameSlug(jsonObject.optString("nameSlug", ""));
        heritage.setDescription(jsonObject.optString("description", ""));
        heritage.setLocation(jsonObject.optString("location", ""));
        heritage.setLocationSlug(jsonObject.optString("locationSlug", ""));
        heritage.setLocationNormalized(jsonObject.optString("locationNormalized", ""));
        heritage.setStatus(jsonObject.optString("status", "ACTIVE"));

        heritage.setCreatedAt(jsonObject.optLong("createdAt", 0));
        heritage.setUpdatedAt(jsonObject.optLong("updatedAt", 0));

        if (jsonObject.has("images")) {
            JSONArray jsonImages = jsonObject.getJSONArray("images");
            List<String> imagesList = new ArrayList<>();

            for (int i = 0; i < jsonImages.length(); i++) {
                imagesList.add(jsonImages.getString(i));
            }

            heritage.setImages(imagesList);
        }

        if (jsonObject.has("coordinates")) {
            JSONObject coordJson = jsonObject.getJSONObject("coordinates");
            Heritage.Coordinates coordinates = new Heritage.Coordinates();

            coordinates.setLatitude(coordJson.optString("latitude", "0"));
            coordinates.setLongitude(coordJson.optString("longitude", "0"));

            heritage.setCoordinates(coordinates);
        }

        if (jsonObject.has("stats")) {
            JSONObject statsJson = jsonObject.getJSONObject("stats");
            Heritage.Stats stats = new Heritage.Stats();

            stats.setAverageRating(statsJson.optString("averageRating", "0"));
            stats.setTotalReviews(statsJson.optString("totalReviews", "0"));
            stats.setTotalVisits(statsJson.optString("totalVisits", "0"));
            stats.setTotalFavorites(statsJson.optString("totalFavorites", "0"));

            heritage.setStats(stats);
        }

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

        if (jsonObject.has("rolePlayIds")) {
            JSONArray idsArray = jsonObject.getJSONArray("rolePlayIds");
            List<String> idsList = new ArrayList<>();

            for (int i = 0; i < idsArray.length(); i++) {
                idsList.add(idsArray.getString(i));
            }

            heritage.setRolePlayIds(idsList);
        }

        if (jsonObject.has("distance")) {
            heritage.setDistance(jsonObject.optDouble("distance", 0.0));
        }

        return heritage;
    }
}