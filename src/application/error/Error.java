package application.error;

public enum Error {
    // ***** DEFINED ERRORS *****
    ACE_TEXT_PASTE(0, "Error pasting text"),
    ACE_TEXT_READ_FILE(1, "Error reading files"),
    APP_PROPERTIES_READ(2, "Error reading application properties"),
    APP_PROPERTIES_XML_PARSE(3, "Error parsing XML"),
    APP_PROPERTIES_SAVE_XML(4, "Error trying to save database config"),
    A_STAR_START_GOAL_MISSING(5, "Error finding start or goal for path"),
    BASH_NODE_PARENT_NULL(6, "Parent BashNode being passed to create new Bash was null"),
    SELENIUM_CLICK_ACTION_NOT_FOUND(7, "ClickAction is failing, selenium is not responding when finding the element"),
    CODE_COMPILE(8, "Error compiling code"),
    CREATE_NODE_MENU_ITEM(9, "Error creating menu for node"),
    COPY_NODE(10, "Error copying node"),
    LOADING_PROGRAM(11, "Error loading program"),
    LOADING_TEST_STEP(12, "Error loading test step"),
    SELECT_QUERY(13, "Error with select query"),
    UPDATE_QUERY(14, "Error with update query"),
    CLOSE_DATABASE_CONNECTION(15, "Error closing connection"),
    OPEN_DATABASE_CONNECTION(16, "Error opening connection to database"),
    PREPARE_QUERY(17, "Error in getting query"),
    PARSING_NODE_CLASS(18, "Error parsing node classes"),
    CLOSE_ZIP(19, "Error closing zip"),
    EMAIL_READ(20, "Error reading email"),
    EMAIL_OPEN_INBOX(21, "Error opening inbox"),
    EMAIL_CHECK(22, "Error checking email"),
    EMAIL_SEND(23, "Error sending email"),
    CREATE_ERROR_WINDOW(24, "Error creating error window"),
    CLOSE_FILE(25, "Error closing file"),
    RUN_EXPORT_NODE(26, "Error exporting node"),
    CREATE_EXPORT_WINDOW(27, "Error creating export window"),
    CREATE_NEW_NODE(28, "Error creating node"),
    START_HTTP_PROXY(29, "Error starting http proxy"),
    HTTP_PROXY_RESPONSE(30, "Error writing http proxy response"),
    IMPORT_NODE(31, "Error importing node"),
    IMPORT_PARSE(32, "Error parsing import"),
    CREATE_IMPORT_WINDOW(33, "Error creating import window"),
    CLOSE_JOBS(34, "Error closing all jobs"),
    JOB_MANAGER(35, "Error creating job manager"),
    SCHEDULE_JOB(36, "Error scheduling job"),
    RUN_LINUX_NODE(37, "Error writing file for linux node"),
    CLOSE_FILE_WRITER(38, "Error closing file writer"),
    RUN_LOGIC_NODE_NEW_INSTANCE(39, "Error creating new instance in logic node"),
    RUN_LOGIC_NODE_INIT(40, "Error finding init method in logic node"),
    NODE_XML_REPRESENTATION(41, "Error trying to create document builder"),
    LOG_OUTPUT(42, "Error decoding log path"),
    LOG_MESSAGE(43, "Error adding log message"),
    CREATE_LOG_WINDOW(44, "Error creating log window"),
    NETWORK_ADDRESS(45, "Error occurred while trying to retrieve network address stack"),
    NETWORK_REACH_HOST(46, "Error occurred while trying to reach host"),
    CREATE_PREVIOUS_TESTS_WINDOW(47, "Error creating previous test window"),
    PROGRAM_JOIN_THREAD(48, "Error joining program threads"),
    REMOTE_DEBUG_COMPILE(49, "Absent information, did you compile your target application with -g option?"),
    REMOTE_DEBUG_EVENT(50, "Error trying to set remote debug event"),
    REMOTE_DEBUG(51, "Error trying to remote debug application"),
    REMOTE_DEBUG_SET_BREAKPOINT(52, "Error setting remote debug break point"),
    CREATE_REQUEST_INSPECT_WINDOW(53, "Error creating request inspect window"),
    CLOSE_FILE_STREAM(54, "Error closing file stream"),
    SCP_COPY(55, "Error running scp copy"),
    TAKE_SCREENSHOT(56, "Error taking screenshot"),
    SDE_FILE_NOT_FOUND(57, "File not found"),
    SDE_JOIN_THREAD(58, "Error joining SDE Thread"),
    FOCAL_BUILD_APPLICATION(59, "Error building application"),
    NODE_IMPLEMENTATION(60, "Error getting node implementation class paths"),
    RESOURCE_PATH(61, "Error getting resource path"),
    RUN_CMD_COMMAND(62, "Error running windows cmd command"),
    KNOWN_HOSTS(63, "Error getting known hosts"),
    SQL_BLOB(64, "Error getting SQL Blob input stream"),
    DESERIALIZE_OBJECT(65, "Error deserializing object"),
    SERIALIZE_OBJECT(66, "Error serializing object"),
    CREATE_SETTINGS_WINDOW(67, "Error creating settings window"),
    CLOSE_SSH_CONNECTION(68, "Error closing SSH Connection"),
    OPEN_SSH_CONNECTION(69, "Error opening SSH Connection"),
    RUN_SSH_COMMAND(70, "Error running SSH Command"),
    SSL_CONTEXT(71, "Error getting SSL Context"),
    HTTP_PROXY_REQUEST(72, "Error running http proxy request"),
    TEST_WAIT_INTERRUPT(73, "Thread interrupt test wait"),
    TEST_NODE_ACTION(74, "Error finding action to run"),
    RETRIEVE_SCREENSHOT(75, "Error getting test screenshot"),
    XML_WRITE_TO_STRING(76, "Error writing XML to string"),
    XML_WRITE_TO_FILE(77, "Error writing XML to file"),
    XML_WRITE_XML_TO_STRING(78, "Error writing xml to string"),
    LOG_APPENDER(79, "Error writing log to file"),
    CREATE_THREAD_WINDOW(80, "Error creating thread window"),
    WAIT_ACTION_TIMEOUT(81, "Error completing wait action"),
    WAIT_ACTION_INTERRUPT(82, "Error interrupted wait action"),
    WRITE_FILE(83, "Error writing to file"),
    WEB_RECORD_SERVER(84, "Error creating web record server"),
    COMPILED_LOGIC_NODE(85, "Error within compiled logic node"),
    SELENIUM_FRAME_NOT_FOUND(86, "Trying to switch to frame that does not exist"),
    DATA_LINK_METHOD_NOT_FOUND(87, "While creating the database model we could not find a required method"),
    DATABASE_OBJECT_METHOD_NOT_FOUND(88, "Could not find method for DatabaseObject when trying to update database"),
    DATABASE_SAVE_CLASS_INIT(89, "Unable to save object"),
    DATABASE_DELETE_CLASS_INIT(90, "Unable to delete object"),
    UUID_CREATE_FAILED(91, "UUID could not be converted from string"),
    DUPLICATE_RESULTS(92, "Duplicate record returned"),
    DATABASE_LOAD_CLASS_INIT(93, "Unable to load object"),
    CREATE_NEW_INSTANCE_ERROR(94, "Unable to create a new instance via the blank constructor"),
    DATABASE_OBJECT_METHOD_MISMATCH_PARAMETERS(95, "The wrong number of arguments have being used"),
    SQLITE_START_EXE(96, "Trouble handling sqlite.exe"),
    DATABASE_TRANSACTION(97, "Database transaction had problems"),
    NODE_CLASS_NOT_FOUND(98, "Could not find node class"),
    DATABASE_MIGRATE_SQL_FAILED(99, "Flyway database migration failed from SQL"),
    DATABASE_REBUILD_FAILED(100, "Database rebuild failed"),
    DATABASE_MIGRATE_FAILED(101, "Flyway database migration failed"),
    HTTP_PROXY_RECEIVE_MESSAGE(102, "Error receiving Http Proxy message"),
    SELENIUM_SELECT_ACTION_NOT_FOUND(103, "SelectAction is failing, selenium is not responding when finding the element"),
    NO_BROWSER_FOUND(104, "Trying to run test with browser that doesn't exist"),
    SELENIUM_JAVASCRIPT_FAILED(105, "Javascript exception seen within browser"),
    HTTP_PROXY_BAD_END_OF_LINE(106, "Error writing http proxy response"),
    SELENIUM_CLICK_ELEMENT_NOT_VISIBLE(107, "Tried to click on an element that was not visible"),
    SSL_EXCEPTION(108, "Unrecognized SSL message"),
    HTTP_TIMEOUT(109, "Http connection has timed out"),
    HTTP_UNKNOWN_HOST(110, "Unknown host for connection"),
    SELENIUM_FAILED_TO_CLOSE_WINDOW(111, "Failed to close browser window"),
    PARSE_DOUBLE_FAILED(112, "Failed to parse Double"),
    PROXY_INTERNAL_SERVER_ERROR(113, "500 returned from request"),
    PROXY_REQUEST_NOT_FOUND(114, "404 returned from request"),
    TEST_ANNOTATION_MISSING(115, "Annotation is missing"),
    TEST_CASE_NO_PROGRAM_SET(116, "The test case is not yet assigned to a program and needs a parent node set before it can be built"),
    TEST_CASE_NODE_TYPE_MISMATCH(117, "A named node is not the type expected"),
    TEST_CASE_CAN_FIND_NODE(118, "A named node cannot be found within the program"),
    TEST_CASE_METHOD_ANNOTATIONS_NOT_FOUND(119, "Method annotations cannot be found, make sure '// METHOD ANNOTATIONS' is in the correct place and present"),
    WEB_DRIVER_EXCEPTION(120, "Web Driver experiences an unexpected problem"),
    NOT_SERIALIZABLE_OBJECT(121, "The current object does not support serialization"),
    CANNOT_RESOLVE_HOST_THIS_MACHINE(122, "Unable to get the named host for the current machine"),
    FAILED_TO_DECODE_GZIP_RESPONSE(123, "Failed to decode a gzip encoded response"),
    PARSE_DATE_FAILED(124, "Failed to parse Date"),
    TEST_CASE_THREAD_WAIT_METHOD_NOT_FOUND(125, "While working with Test Case, thread wait method could not be found"),
    DOCUMENT_CREATION_ISSUE(126, "Unable to complete creating document from test"),
    XPATH_GENERATION(127, "Unable to generate xPath of element"),
    SELENIUM_INPUT_ACTION_NOT_FOUND(128, "InputAction is failing, selenium is not responding when finding the element");

    private Integer code;
    private String description;

    Error(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public RecordedError record() {
        return new RecordedError(code, description, this.name());
    }
}
