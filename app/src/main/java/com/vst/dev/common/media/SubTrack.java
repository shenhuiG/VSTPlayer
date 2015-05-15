package com.vst.dev.common.media;



public class SubTrack {
    public String name;
    public String path;
    public int trackId;
    public SubTrackType from;
    public String language;

    public SubTrack(){}

    public SubTrack(SubTrackType from){
        this.from=from;
    }

    @Override
    public boolean equals(Object o) {
        if( o instanceof SubTrack){
            SubTrack _track =(SubTrack) o;
            if(_track.from == from){
                switch (from){
                    case Local:
                    case Internet:
                        return _track.path .equals(path);
                    case Internal:
                        return _track.trackId == trackId;
                    case NONE:
                        return true;
                }
            }
        }
        return false;
    }

    @Override
    public String toString() {
        switch (from){
            case Local:
            case Internet:
                return "zimu"+path;
            case Internal:
                return "zimu"+trackId;
            case NONE:
                return "WUZIMU";
        }
        return null;
    }

    public static enum SubTrackType {
        Local, Internal, Internet,NONE
    }
}
