package org.sab.chat.server.routers;

public class RouterBuilder {

    public static Router createRouter(String routerType) {
        return switch (routerType) {
            case "AUTH" -> new AuthRouter();
            case "ADD_GROUP_MEMBER" -> new AddMemberRouter();
            case "REMOVE_GROUP_MEMBER" -> new RemoveMemberRouter();
            case "LEAVE_GROUP" -> new LeaveGroupRouter();
            case "GET_DIRECT_MESSAGES" -> new GetDirectMessagesRouter();
            case "GET_GROUP_MESSAGES" -> new GetGroupMessagesRouter();
            case "CREATE_GROUP_MESSAGE" -> new CreateGroupMessageRouter();
            case "CREATE_GROUP_CHAT" -> new CreateGroupChatRouter();
            case "CREATE_DIRECT_CHAT" -> new CreateDirectChatRouter();
            case "CREATE_DIRECT_MESSAGE" -> new CreateDirectMessageRouter();
            default -> null;
        };
    }
}
