package net.kyma;

public enum EventType
{
   PLAYER_PLAY,
   PLAYER_PLAY_FROM,
   PLAYER_STOP,
   PLAYER_PAUSE,
   PLAYER_RESUME,
   PLAYER_SET_SLIDER,
   PLAYER_STOP_TIMER,

   PLAYLIST_ADD_FILE,
   PLAYLIST_ADD_SOUND,
   PLAYLIST_REMOVE_SOUND,
   PLAYLIST_ADD_LIST,
   PLAYLIST_REMOVE_LIST,
   PLAYLIST_NEXT,
   PLAYLIST_PREVIOUS,
   PLAYLIST_HIGHLIGHT,

   DATA_INDEX_DIRECTORY,
   DATA_INDEX_LIST,
   DATA_INDEX_ITEM,
   DATA_INDEX_GET_ALL,
   DATA_INDEX_GET_DISTINCT,
   DATA_INDEX_GET_DIRECTORIES,
   DATA_STORE_ITEM,
   DATA_STORE_LIST,
   DATA_REMOVE_ITEM,
   DATA_CONVERT_FROM_DOC,

   DATA_INDEXING_AMOUNT,
   DATA_INDEXING_PROGRESS,
   DATA_INDEXING_FINISH,
   DATA_REFRESH,
   DATA_UPDATE_REQUEST,

   DATA_SET_DISTINCT_PEOPLE,
   DATA_SET_DISTINCT_MOOD,
   DATA_SET_DISTINCT_OCCASION,
   DATA_SET_DISTINCT_TEMPO,
   DATA_SET_DISTINCT_GENRE,
   DATA_SET_DISTINCT_INSTRUMENT,
   DATA_SET_DISTINCT_CUSTOM1,
   DATA_SET_DISTINCT_CUSTOM2,
   DATA_SET_DISTINCT_CUSTOM3,
   DATA_SET_DISTINCT_CUSTOM4,
   DATA_SET_DISTINCT_CUSTOM5,

   FILES_REMOVE,

   GUI_WINDOW_SETTINGS,
   GUI_WINDOW_SET_MAXIMIZED,
   GUI_WINDOW_SET_FULLSCREEN,
   GUI_WINDOW_SET_FRAME,

   PROPERTIES_STORE_WINDOW_FULLSCREEN,
   PROPERTIES_STORE_WINDOW_MAXIMIZED,
   PROPERTIES_STORE_WINDOW_FRAME,

   //Container events
   RET_CONTENT_VIEW,

   CLOSE
}
