export const COMMANDS = {
    OPEN_FILE_IN_EDITOR: 'open-file-in-editor',
    OPEN_CUSTOM_EDITOR_TAB: 'open-custom-editor-tab',
    UNDO: 'undo',
    REDO: 'redo',
    ACTIVATE_EDITOR_FOR_FILE: 'activate-editor-for-file',
    FORMAT: 'source-format',
};

export const EVENTS = {
    UPDATE_TAB_TITLE: 'update-tab-title',
    UNDO_EVENT: 'undo-event',
    REDO_EVENT: 'redo-event',
};

export const VIEWS = {
    EDITOR_TABS: 'composer.view.editor.tabs',
};


export const DIALOGS = {
    DIRTY_CLOSE_CONFIRM: 'composer.dialog.editor.dirty-file-close-confirm',
    OPENED_FILE_DELETE_CONFIRM: 'composer.dialog.editor.opened-file-delete-confirm',
};

export const MENUS = {
    EDIT: 'composer.menu.editor.edit',
    UNDO: 'composer.menu.editor.undo',
    REDO: 'composer.menu.editor.redo',
    FORMAT: 'composer.menu.editor.format',
};

export const TOOLS = {
    UNDO_REDO_GROUP: 'composer.tool.group.editor.undo-redo',
    CODE_GROUP: 'composer.tool.group.editor.code ',
    UNDO: 'composer.tool.editor.undo',
    REDO: 'composer.tool.editor.redo',
    FORMAT: 'composer.tool.editor.format',
};

export const LABELS = {
    EDIT: 'Edit',
    UNDO: 'Undo',
    REDO: 'Redo',
    FORMAT: 'Reformat Code',
};

export const HISTORY = {
    ACTIVE_EDITOR: 'composer.plugin.editor.active-editor',
    PREVIEW_VIEW_IS_ACTIVE: 'preview-panel-is-active',
    PREVIEW_VIEW_PANEL_SIZE: 'preview-view-pane-size',
};

export const PLUGIN_ID = 'composer.plugin.editor';
