package org.sab.chat.server.routers;

public class RouterBuilder {

    public static Router createRouter(String routerType) {
        return switch (routerType) {
            case "Auth" -> new AuthRouter();
            case "AddGroupMember" -> new AddMemberRouter();
            case "RemoveGroupMember" -> new RemoveMemberRouter();
            case "LeaveChat" -> new LeaveChatRouter();
            case "GetDirectMessages" -> new GetDirectMessagesRouter();
            case "GetGroupMessages" -> new GetGroupMessagesRouter();
            case "CreateGroupMessage" -> new CreateGroupMessageRouter();
            case "CreateGroupChat" -> new CreateGroupChatRouter();
            case "CreateDirectChat" -> new CreateDirectChatRouter();
            case "CreateDirectMessage" -> new CreateDirectMessageRouter();
            default -> null;
        };
    }
}
