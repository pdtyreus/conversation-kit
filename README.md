# Conversation Kit

**A directed graph model for conversational UIs :speech_balloon:**

Created as a spare time project by P. Daniel Tyreus - [@tyreus](https://www.twitter.com/tyreus)

 * Minimal dependencies
 * Well documented
 * Clear, concise API designed to be extended and customized
 * Easy to [test](https://gist.github.com/pdtyreus/b599e40b3a94fba3b80ca5fdd63f11b3)

## Introduction

Conversation kit aims to provide a flexible structure for processing conversations
between a human user and a chat bots or similar autonomous agent. This project
takes the approach of modeling a conversation with a chat bot as a directed graph.
The nodes of the graph (or vertices if you prefer) are the conversation snippets
spoken by the bot. The edges of the graph represent the flow of the conversation
snippets spoken by the bot by connecting one node to another.

Below is an example of a specialized form of a directed graph conversation known
as a [dialog tree](https://en.wikipedia.org/wiki/Dialog_tree). In this case each
node spoken by the bot requires a response from the user. Each edge directs the
conversation to the next node based on the response chosen.

![Dialog Tree](https://upload.wikimedia.org/wikipedia/commons/3/31/Dialog_tree_example.svg)

## Directed Conversations

Conversation kit takes a more generalized approach to modeling conversations. A
`DirectedConversationEngine` starts with an initial state that specifies a start
node. The engine looks at all the outbound edges from the start node and picks
the first one that returns `true` for it's `isMatchForState()` method. The engine
then proceeds to the end node for the matching edge and continues to run until
it reaches a node that requires input from the user or with no matching
outbound edges.

```java
JsonGraphBuilder<TestCaseUserState> builder = new JsonGraphBuilder();
DirectedConversationEngine<TestCaseUserState> engine = builder.readJsonGraph("/directed_conversation.json");
TestCaseUserState state = new TestCaseUserState();

// run the engine from the starting state to an endpoint
Iterable<IConversationSnippet> snippets = engine.startConversationFromState(state);
```

You would then send the snippets as messages from the bot using your preferred
chat client. Once the user responds to the bot, you would update the state
with the response and let the engine run again.

```java
try {
    state = engine.updateStateWithResponse(state, response);
} catch (UnmatchedResponseException e) {

}
snippets = engine.startConversationFromState(state);
```

## Conversation State

The conversation state is a data store designed to persist a user's progress
through a conversation, help customize the messages sent by the bot to the
user, and to save data from user responses during the conversation. In many
cases the implementation will be backed by a database or other permanent
storage.

Conversation Kit ships with a simple `IConversationState` [implementation](src/main/java/com/synclab/conversationkit/impl/MapBackedState.java) backed
by a `HashMap`.

## Nodes

A conversation node is a vertex on the directed conversation graph containing
content for the bot to present to the user. Each node has
zero or more outbound edges and zero or more inbound edges. The conversation
traverses the graph between nodes in by analyzing the state and choosing
the first matching edge at each vertex.

Each node contains a conversation snippet represents a small bit of dialog in a conversation.
In the case of a chat bot, this might represent a block of text sent as one
message. Snippets can be classified as a `STATEMENT`
or `QUESTION`. Generally, if the snippet is a
`STATEMENT` the conversation will proceed to the next node
automatically. If it is a `QUESTION`, the conversation will
stop and wait for a response from the user. However, this depends on
the `IConversationEngine` implementation.

```java
public interface IConversationSnippet<S extends IConversationState> {
    public String renderContent(S state);
    public SnippetType getType();
    public Iterable<String> getSuggestedResponses();
}
```

The `renderContent` method receives the current conversation state so that the
displayed message can be modified at runtime. For example, you could use a
template engine to swap in the user's name in the messages. See
[TemplatedDialogTreeNode](src/test/java/com/synclab/conversationkit/impl/TemplatedDialogTreeNode.java)
in the tests as an example.

Nodes can also suggest possible responses. Some chat bot
clients like Facebook Messenger support displaying suggested responses in
the interface while others like Slack do not.

There are only two `IConversationNode` implementations currently in the code,
but it's easy to create your own by extending the abstract class [ConversationNode](src/main/java/com/synclab/conversationkit/impl/node/ConversationNode.java).

### DialogTreeNode

A dialog tree is a type of branching conversation often seen in adventure
video games. The user is given a choice of what to say and makes subsequent
choices until the conversation ends. The responses to the user are scripted
based on the choices made. A Dialog Tree would be a choice to model a
conversation when your UI does not allow free-form responses, like a
questionnaire.

A `DialogTreeNode` is a restricted implementation of
`IConversationNode` that
holds a text string to represent the displayed conversation snippet and
retrieves a list of allowed responses from the outbound edges. There is a
working example of how to model, build, and use a Dialog Tree in the
[test classes](src/test/java/com/synclab/conversationkit/impl/DialogTreeTest.java).

### ResponseSuggestingNode

A `ResponseSuggestingNode` is a more general implementation of `IConversationNode`.
If the node type is a `QUESTION`, it can also hold a list of "suggested" responses.
Some chat interfaces like Facebook Messenger let you send suggested answers
along with a question from the bot.

## Edges

A conversation edge is a directed connection between two nodes on the
conversation graph. Each edge has exactly one start node and one end node,
but a node frequently has multiple outbound edges. The conversation
implementation will look at each outbound edge from a node to decide which
edge to use to continue traversing the conversation graph.

```java
public interface IConversationEdge<S extends IConversationState> {
    public IConversationNode<S> getEndNode();
    public boolean isMatchForState(S state);
    public void onMatch(S state);
}
```

A conversation will continue to the end node of a given edge if that edge is
the first to return a `true` value from `isMatchForState()`. The edge can also
choose to modify the state after it matches in `onMatch()`.

### StatementEdge

A simple `IConversationEdge` implementation that always returns true
from `isMatchForState()` matches. This
implementation would be best used for connecting multiple statement type nodes
that should always be spoken by the bot in sequence.

### DialogTreeEdge

A `DialogTreeEdge` is an implementation of `IConversationEdge`
that connects one
`IConversationNode` that is a `QUESTION` to the
`IConversationNode` matching the answer. It is designed to be used with
`DialogTreeNodes` in a dialog tree type conversation.

```java
public boolean isMatchForState(S state) {
    return answer.equals(state.getCurrentResponse());
}
```

### RegexEdge

An edge type that matches responses based on a regular expression pattern.
If a `stateKey` is
provided, the `onMatch()` method sets the value of this key in
the conversation state equal to the first group found in the match.

```java
public boolean isMatchForState(S state) {
    Matcher matcher = pattern.matcher(state.getCurrentResponse());
    return matcher.find();
}

public void onMatch(S state) {
    Matcher matcher = pattern.matcher(state.getCurrentResponse());
    if ((stateKey != null) && matcher.find()) {
        state.set(stateKey, matcher.group());
    }
}
```

### JavaScriptEdge

An IConversationEdge implementation that delegates matching logic to external
JavaScript code. Similar to a `RegexEdge`, this type of edge
allows users to store the logic for determining if an edge matches in a
location outside the source code. For instance, the string representation
of the JavaScript logic could be stored in a database or file representation
of the conversation graph.

The supplied JavaScript code modifies the behavior of the
`isMatchForState()`
and `onMatch()`
methods. The string representation of the JavaScript code is wrapped as
follows:

```javascript
function isMatchForState(state) {
  eval(isMatchForState);
}

function onMatch(state) {
  eval(onMatch);
}
```

So, for example, if
```java
isMatchForState = "return (state.currentResponse === 'graph');"
```
then the IConversation implementation would evaluate the result of

```javascript
function isMatchForState(state) {
  return (state.currentResponse === 'graph');
}
```
to determine if the edge matches the current state.

## Putting It All Together

For an example of a reasonably complex conversation graph with multiple node
types and edges all loaded from a JSON file, see
[ConversationGraphTest](src/test/java/com/synclab/conversationkit/impl/ConversationGraphTest.java).

I also have a [gist](https://gist.github.com/pdtyreus/b599e40b3a94fba3b80ca5fdd63f11b3) that
runs a ultra-simple chat on the console, but can be extended into a nice testbed.

If you have questions or suggestions, you can contact me
[on Twitter](https://www.twitter.com/tyreus).
