package org.csanchez.aws.glacier;

public enum Action {

    UPLOAD, DOWNLOAD, DELETE, INVENTORY;

    public static Action fromName( String name ) {
        for ( Action action : values() ) {
            if ( action.name().equalsIgnoreCase( name ) ) return action;
        }

        return null;
    }

}
