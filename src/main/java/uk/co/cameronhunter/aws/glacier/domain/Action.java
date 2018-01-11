package uk.co.cameronhunter.aws.glacier.domain;

public enum Action {

    UPLOAD, DOWNLOAD, DELETE, INVENTORY, VAULTS;

    public static Action fromName(String name) {
        for (Action action : values()) {
            if (action.name().equalsIgnoreCase(name)) return action;
        }

        return null;
    }

}
