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
   PLAYER_SET_VOLUME,

   PLAYLIST_ADD_FILE,
   PLAYLIST_ADD_SOUND,
   PLAYLIST_REMOVE_SOUND,
   PLAYLIST_ADD_LIST,
   PLAYLIST_REMOVE_LIST,
   PLAYLIST_NEXT,
   PLAYLIST_PREVIOUS,
   PLAYLIST_REFRESH,

   /** content {@link java.io.File}*/
   DATA_INDEX_DIRECTORY,
   DATA_INDEX_LIST,
   DATA_INDEX_ITEM,
   DATA_INDEX_GET_ALL,
   /** content {@link net.kyma.dm.SupportedField} <br>
    * produces {@link java.util.Set}&lt{@link String}&gt*/
   DATA_INDEX_GET_DISTINCT,
   DATA_INDEX_GET_DIRECTORIES,
   /** empty <br> resend {@link EventType#DATA_REFRESH_PATHS}*/
   DATA_GET_PATHS,
   /** content Map &lt String, Set &lt String &gt> */
   DATA_REFRESH_PATHS,
   DATA_STORE_ITEM,
   DATA_STORE_LIST,
   DATA_REMOVE_ITEM,
   /** content {@link net.kyma.data.PathQueryParameters}*/
   DATA_REMOVE_PATH,
   /** accept {@link java.util.Collection} of {@link org.apache.lucene.document.Document}
    * produces {@link java.util.Collection} of {@link net.kyma.dm.SoundFile}*/
   DATA_CONVERT_FROM_DOC,
   /** empty */
   DATA_INDEX_CLEAN,

   DATA_INDEXING_AMOUNT,
   DATA_INDEXING_PROGRESS,
   DATA_INDEXING_FINISH,
   DATA_REFRESH,
   /** accepts {@link net.kyma.dm.TagUpdateRequest}*/
   DATA_UPDATE_REQUEST,

   /** accept {@link net.kyma.data.QueryParameters}
    * produces {@link java.util.Collection}&lt{@link net.kyma.dm.SoundFile}&gt*/
   DATA_QUERY,
   /** accept {@link java.util.Collection}&lt{@link net.kyma.dm.SoundFile}&gt*/
   DATA_QUERY_RESULT_FOR_CONTENT_VIEW,

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
   GUI_VOLUME_GET,
   GUI_VOLUME_SET,
   GUI_CONTENTVIEW_SETTINGS_CHANGED,
   GUI_CONTENTVIEW_SETTINGS_STORE,
   GUI_CONTENTVIEW_SETTINGS_GET,
   GUI_CONTENTVIEW_SETTINGS_SET,

   PROPERTIES_STORE_WINDOW_FULLSCREEN,
   PROPERTIES_STORE_WINDOW_MAXIMIZED,
   PROPERTIES_STORE_WINDOW_FRAME,

   //Container events
   RET_CONTENT_VIEW,
   RET_OBJECT_MAPPER,
   RET_INDEX_WRITER,
   RET_SOUND_FILE_CONVERTER,
   RET_DOC_CONVERTER,
   RET_MAIN_WINDOW,
   RET_CONTROLLER_DISTRIBUTOR,

   SHOW_ALERT,

   CLOSE
}
