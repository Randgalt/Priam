package com.netflix.priam.agent;

import com.netflix.priam.agent.process.AgentProcessManager;
import com.netflix.priam.agent.process.AgentProcessMap;
import com.netflix.priam.agent.tasks.AgentTask;
import com.netflix.priam.agent.tasks.ProcessTask;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

import static com.netflix.priam.agent.tasks.ProcessTaskConstants.*;

public class TestTasks
{
    @Test
    public void testAgentTaskBasic() throws Exception
    {
        MockNodeStatus nodeStatus = new MockNodeStatus();
        JSONObject testInfo = new JSONObject();
        testInfo.put("a", "a");
        testInfo.put("b", "b");
        testInfo.put("c", "c");
        nodeStatus.setInfo(testInfo);

        MockStorage storage = new MockStorage();

        MockAgentConfiguration configuration = new MockAgentConfiguration("localhost");
        AgentProcessManager processManager = new AgentProcessManager(new AgentProcessMap(AgentProcessMap.buildDefaultMap()), configuration, nodeStatus);
        AgentTask agentTask = new AgentTask(configuration, processManager, nodeStatus, storage);
        agentTask.execute();

        String value = storage.getData().values().iterator().next();
        JSONObject obj = new JSONObject(value);
        JSONObject info = obj.getJSONObject("info");
        Assert.assertNotNull(info);
        Assert.assertEquals(info.get("a"), "a");
        Assert.assertEquals(info.get("b"), "b");
        Assert.assertEquals(info.get("c"), "c");
    }

    @Test
    public void testProcessTaskBasic() throws Exception
    {
        JSONObject commandObject = new JSONObject();
        commandObject.put(FIELD_COMMAND, COMMAND_START);
        commandObject.put(FIELD_ID, "1");
        commandObject.put(FIELD_NAME, "compact");
        commandObject.put(FIELD_ARGUMENTS, new JSONArray());

        JSONArray   commandTab = new JSONArray();
        commandTab.put(commandObject);

        MockAgentConfiguration configuration = new MockAgentConfiguration("localhost");

        MockStorage storage = new MockStorage();
        storage.setValue(configuration, ProcessTask.ROW_KEY, "localhost", commandTab.toString());

        MockNodeStatus nodeStatus = new MockNodeStatus();
        AgentProcessManager processManager = new AgentProcessManager(new AgentProcessMap(AgentProcessMap.buildDefaultMap()), configuration, nodeStatus);
        ProcessTask processTask = new ProcessTask(configuration, processManager, storage);
        processTask.execute();
        Assert.assertTrue(processManager.closeAndWaitForCompletion(5, TimeUnit.SECONDS));

        Assert.assertEquals(nodeStatus.getOperations().size(), 1);
        Assert.assertEquals(nodeStatus.getOperations().get(0), "compact");

        Assert.assertEquals(processManager.getActiveProcesses().size(), 0);
    }
}