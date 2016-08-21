package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import models.Profile;
import models.User;
import play.data.DynamicForm;
import play.data.FormFactory;
import play.mvc.Controller;
import play.mvc.Result;

import javax.inject.Inject;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Created by lubuntu on 8/21/16.
 */
public class HomeController extends Controller {
    @Inject
    ObjectMapper objectMapper;

    @Inject
    FormFactory formFactory;

    public Result getProfile(Long userId) {
        User user = User.find.byId(userId);
        Profile profile = Profile.find.byId(user.profile.id);
        ObjectNode data = objectMapper.createObjectNode();

        List<Long> connectedUserIds = user.connections.stream().map(x -> x.id).collect(Collectors.toList());
        List<Long> connectionRequestSentUserIds = user.connectionRequestsSent.stream().map(x -> x.receiver.id).
                collect(Collectors.toList());

        List<JsonNode> suggestions = User.find.all().stream().filter(x -> !connectedUserIds.contains(x.id)
                && !connectionRequestSentUserIds.contains(x.id)
                && !Objects.equals(x.id, userId))
                .map(userElement -> {
                    Profile profile1 = Profile.find.byId(userElement.profile.id);
                    ObjectNode userJson = objectMapper.createObjectNode();
                    userJson.put("email", userElement.email);
                    userJson.put("id", userElement.id);
                    userJson.put("firstName", profile1.firstName);
                    userJson.put("lastName", profile1.lastName);
                    return userJson;
                })
                .collect(Collectors.toList());

        data.set("suggestions", objectMapper.valueToTree(suggestions));

        List<JsonNode> connections = user.connections.stream().map(x -> {
            User connectedUser = User.find.byId(x.id);
            Profile profile1 = Profile.find.byId(connectedUser.profile.id);
            ObjectNode userJson = objectMapper.createObjectNode();
            userJson.put("email", connectedUser.email);
            userJson.put("id", connectedUser.id);
            userJson.put("firstName", profile1.firstName);
            userJson.put("lastName", profile1.lastName);
            return userJson;

        }).collect(Collectors.toList());

        data.set("connections", objectMapper.valueToTree(connections));

        List<JsonNode> connectionRequestsReceived = user.connectionRequestsReceived.stream().map(x -> {
            User requestor = User.find.byId(x.sender.id);
            Profile requestorProfile = Profile.find.byId(requestor.profile.id);
            ObjectNode requestorJson = objectMapper.createObjectNode();
            requestorJson.put("email", requestor.email);
            requestorJson.put("id", requestor.id);
            requestorJson.put("firstName", requestorProfile.firstName);
            requestorJson.put("lastName", requestorProfile.lastName);
            return requestorJson;

        }).collect(Collectors.toList());

        data.set("connectionRequestsReceived", objectMapper.valueToTree(connectionRequestsReceived));

        return ok(data);
    }

    public Result updateProfile(Long userId){
        DynamicForm form = formFactory.form().bindFromRequest();
        User user = User.find.byId(userId);
        Profile profile = Profile.find.byId(user.profile.id);
        profile.company = form.get("company");
        profile.firstName = form.get("firstName");
        profile.lastName = form.get("lastName");
        Profile.db().update(profile);
        return ok();
    }
}
