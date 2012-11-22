/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.mms.templates;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import com.android.mms.R;
import com.android.mms.templates.TemplatesProvider.Template;
import com.android.mms.ui.ConversationList;

public class TemplatesListActivity extends Activity implements
        LoaderManager.LoaderCallbacks<Cursor> {

    // codes for dialogs
    private static final int DIALOG_CANCEL_CONFIRM = 2;

    private static final int LOAD_TEMPLATES = 1;

    private ListView mListView;

    private long mTemplateToDeleteId;

    private SimpleCursorAdapter mAdapter;

    protected void createNewTemplate() {
        final Intent intent = new Intent(this, TemplateEditor.class);
        intent.putExtra(TemplateEditor.KEY_DISPLAY_TYPE, TemplateEditor.DISPLAY_TYPE_NEW_TEMPLATE);
        startActivity(intent);
    }

    protected void doDeleteTemplate() {
        Uri uriToDelete = ContentUris
                .withAppendedId(Template.CONTENT_URI, mTemplateToDeleteId);
        getContentResolver().delete(uriToDelete, null, null);
        TemplateGesturesLibrary.getStore(this).removeEntry(String.valueOf(mTemplateToDeleteId));
    }

    protected void modifyTemplate(long id) {
        final Intent intent = new Intent(this, TemplateEditor.class);
        intent.putExtra(TemplateEditor.KEY_DISPLAY_TYPE, TemplateEditor.DISPLAY_TYPE_EDIT_TEMPLATE);
        intent.putExtra(TemplateEditor.KEY_TEMPLATE_ID, id);
        startActivity(intent);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {

        final int itemId = item.getItemId();

        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item
                .getMenuInfo();

        switch (itemId) {
            case R.id.template_delete:
                mTemplateToDeleteId = info.id;
                showDialog(DIALOG_CANCEL_CONFIRM);
                break;

            case R.id.template_edit:
                modifyTemplate(info.id);
                break;
        }
        return super.onContextItemSelected(item);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.templates_list);

        mListView = (ListView) findViewById(R.id.template_lv);

        getLoaderManager().initLoader(LOAD_TEMPLATES, null, this);

        mAdapter = new SimpleCursorAdapter(this,
                android.R.layout.simple_list_item_1, null, new String[] {
                        Template.TEXT
                }, new int[] {
                        android.R.id.text1
                }, CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);

        mListView.setAdapter(mAdapter);
        View emptyView = findViewById(R.id.empty);
        mListView.setEmptyView(emptyView);
        mListView.setOnItemClickListener(new OnItemClickListener() {

            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long id) {
                modifyTemplate(id);
            }
        });

        getActionBar().setDisplayHomeAsUpEnabled(true);

        registerForContextMenu(mListView);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {

        if (v == mListView) {
            getMenuInflater().inflate(R.menu.templates_list_context_menu, menu);
            menu.setHeaderTitle(R.string.template_ctx_menu_title);
        }

        super.onCreateContextMenu(menu, v, menuInfo);
    }

    @Override
    protected Dialog onCreateDialog(int id, Bundle args) {

        switch (id) {
            case DIALOG_CANCEL_CONFIRM:
                final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(R.string.template_cancel_confirm_title);
                builder.setMessage(R.string.template_cancel_confirm_text);
                builder.setPositiveButton(R.string.yes, new Dialog.OnClickListener() {

                    public void onClick(DialogInterface arg0, int arg1) {
                        doDeleteTemplate();
                    }
                });
                builder.setNegativeButton(R.string.no, new Dialog.OnClickListener() {

                    public void onClick(DialogInterface dialog, int arg1) {
                        dialog.dismiss();
                    }
                });
                builder.setCancelable(true);
                return builder.create();

            default:
                break;
        }

        return super.onCreateDialog(id, args);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.template_list_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Intent intent = new Intent(this, ConversationList.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                return true;
            case R.id.action_template_new:
                createNewTemplate();
                return true;
            default:
                return false;
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(this, Template.CONTENT_URI, null, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.swapCursor(null);
    }

}
