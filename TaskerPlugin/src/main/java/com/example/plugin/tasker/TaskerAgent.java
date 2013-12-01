package com.example.plugin.tasker;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.preference.PreferenceManager;

import mobi.voiceassistant.base.Request;
import mobi.voiceassistant.base.Response;
import mobi.voiceassistant.base.Token;
import mobi.voiceassistant.base.content.Utterance;
import mobi.voiceassistant.client.AssistantAgent;
import mobi.voiceassistant.client.content.Bubble;

/**
 * Created by morfeusys on 01.12.13.
 */
public class TaskerAgent extends AssistantAgent {

    private static final String TOKEN_COMMAND = "Command";
    private static final String TOKEN_TASK = "Task";

    private static final String COOKIE_COMMAND = "COOKIE_COMMAND";

    @Override
    protected void onCommand(Request request) {
        switch (request.getDispatchId()) {
            case R.id.cmd_tasker_create:
                onCreateTask(request);
                break;
            case R.id.cmd_tasker_task:
                onTask(request);
                break;
            case R.id.cmd_tasker_delete:
                onDelete(request);
                break;
        }
    }

    @Override
    protected void onModalCancel(Request request) {
        request.addQuickResponse(getString(R.string.canceled));
    }

    @Override
    protected void onBubbleClick(Request request) {
        switch (request.getDispatchId()) {
            case R.id.btn_select_task:
                onSelectTask(request);
                break;
        }
    }

    private void onSelectTask(Request request) {
        if(TaskerIntent.taskerInstalled(this)) {
            startActivityForResult(request, null, TaskerIntent.getTaskSelectIntent(), R.id.btn_select_task);
        } else {
            request.addQuickResponse(getString(R.string.task_not_selected));
        }
    }

    private void onCreateTask(Request request) {
        final Response response = request.createResponse();
        final Token token = request.getContent();
        final Token commandToken = token.findTokenByName(TOKEN_COMMAND);

        final String command = commandToken.getSource().trim();

        if(command.length() == 0) {
            response.setContent(getString(R.string.say_command));
            response.enterModalQuestionScope(R.xml.mod_tasker_command);
        } else {
            String task = findTask(command);
            if(task != null) {
                runTask(task);
                response.setContent(getString(R.string.execute_task, task));
            } else {
                Bubble bubble = new Bubble(R.layout.select_command);
                bubble.setTextViewText(R.id.command, command);
                bubble.makeOnClickRequest(R.id.btn_select_task);
                response.setContent(new Utterance(bubble, getString(R.string.speech_select_task)));
                response.putCookie(Response.COOKIE_MODE_AGENT, COOKIE_COMMAND, command);
            }
        }

        request.addResponse(response);
    }

    @Override
    protected void onActivityResult(Request request, Response response, int requestCode, int resultCode, Intent data) {
        if(resultCode == Activity.RESULT_OK) {
            final String command = request.getStringCookie(COOKIE_COMMAND);
            final String task = data.getDataString();
            SharedPreferences preferences = getSharedPreferences(TaskContentProvider.PREFERENCES, Context.MODE_PRIVATE);
            preferences.edit().putString(command, task).commit();
            request.addQuickResponse(getString(R.string.task, command, task));
        } else {
            request.addQuickResponse(getString(R.string.task_not_selected));
        }

    }

    private void onTask(Request request) {
        final Token token = request.getContent();
        final Token taskToken = token.findTokenByName(TOKEN_TASK);

        final String task = taskToken.getValue();

        request.addQuickResponse(getString(R.string.execute_task, task));

        runTask(task);
    }

    private void onDelete(Request request) {
        final Response response = request.createResponse();
        final Token token = request.getContent();
        final Token commandToken = token.findTokenByName(TOKEN_COMMAND);

        final String command = commandToken.getSource().trim();

        if(command.length() == 0) {
            response.setContent(getString(R.string.command_to_delete));
            response.enterModalQuestionScope(R.xml.mod_tasker_delete);
        } else {
            SharedPreferences preferences = getSharedPreferences(TaskContentProvider.PREFERENCES, Context.MODE_PRIVATE);
            preferences.edit().remove(command).commit();
            response.setContent(getString(R.string.command_deleted, command));
        }

        request.addResponse(response);
    }

    private String findTask(String command) {
        Cursor cur = getContentResolver().query(Uri.parse("content://net.dinglisch.android.tasker/tasks"), null, null, null, null);
        if(cur != null) {
            try {
                final int nameCol = cur.getColumnIndex("name");
                while (cur.moveToNext()) {
                    String name = cur.getString(nameCol);
                    if(name.equalsIgnoreCase(command)) {
                        return name;
                    }
                }
            } finally {
                cur.close();
            }
        }
        return null;
    }

    private void runTask(String task) {
        if (TaskerIntent.testStatus(this).equals(TaskerIntent.Status.OK)) {
            TaskerIntent intent = new TaskerIntent(task);
            sendBroadcast(intent);
        }
    }
}
