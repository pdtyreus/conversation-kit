# Conversation Kit

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Maven Central](https://img.shields.io/maven-central/v/com.conversationkit/conversation-kit.svg)](https://mvnrepository.com/artifact/com.conversationkit/conversation-kit)

**A directed graph model for conversational UIs :speech_balloon:**

Created as a spare time project by P. Daniel Tyreus - [@tyreus](https://www.twitter.com/tyreus)

 * Minimal dependencies
 * Well [documented](http://www.javadoc.io/doc/com.conversationkit/conversation-kit)
 * Clear, concise API designed to be extended and customized
 * Redux-style state management for predictability and easier testing.

## Introduction

Conversation kit aims to provide a flexible structure for processing conversations
between a human user and a chat bots ands voice agents. This project
takes the approach of modeling a conversation as a directed graph.
The nodes of the graph (or vertices if you prefer) are the conversation snippets
spoken by the bot. The edges of the graph represent the flow of the conversation based on
the interpreted intent of user by connecting one node to another.

Below is an example of a specialized form of a directed graph conversation known
as a [dialog tree](https://en.wikipedia.org/wiki/Dialog_tree). In this case each
node spoken by the bot requires a response from the user. Each edge directs the
conversation to the next node based on the response chosen.

![Dialog Tree](https://upload.wikimedia.org/wikipedia/commons/3/31/Dialog_tree_example.svg)

Conversations can quickly get more complicated, with loops and multi-level flow. Below
is an example of the conversation graph for a chatbot to [log allergy symptoms](https://hayfever.io).

![Conversation Graph](https://cdn-images-1.medium.com/max/2600/1*Vd-3Oy-VGCCdERzMescj9g.png)

## Installation

The artifacts are available on Maven Central

```xml
<dependency>
  <groupId>com.conversationkit</groupId>
  <artifactId>conversation-kit</artifactId>
  <version>2.0.0</version>
</dependency>
```

## Directed Conversations

Conversation kit takes a more generalized approach to modeling conversations. A
`DirectedConversationEngine` starts with an initial state that specifies a start
node. The engine accepts a message from a user and delegates to a natural language 
understanding (NLU) system to determine the user's intent. The `DirectedConversationEngine`
then looks at all the outbound edges from the start node and picks
the first one that matches the intent and returns `true` for it's `validate()` method. The engine
then proceeds to the target node for the matching edge and waits for the next user input.

```java
//handle an incoming message from a user
MessageHandlingResult result = engine.handleIncomingMessage("hello").get();

//get the node that the conversation has progressed to
ConversationNode currentNode = index.getNodeById(engine.getState().getCurrentNodeId());
```

You would then check to see what action the current node is set to perform or 
what messages it should send. This will depend on the node implementation, but
a simple example to get a list of messages for the bot to respond with might look like:

```java
for (JsonValue message : currentNode.getMetadata().get("message").asArray()) {
    String m = message.asString();
    //send m
}
```

### JSON Conversation Graphs

This project uses [JSON Graph Format](http://jsongraphformat.info/) to store the
graph representation of conversations. Conversation Kit provides some classes
for reading JSON in this format and creating the internal graph representation.
You can use format you prefer by writing your own `JsonGraphBuilder` implementation.

## Conversation State

The conversation state is a data store designed to persist a user's progress
through a conversation, help customize the messages sent by the bot to the
user, and to save data from user responses during the conversation. In many
cases the implementation will be backed by a database or other permanent
storage.

Conversation Kit ships with an abstract `IConversationState` 
[implementation](src/main/java/com/synclab/conversationkit/impl/MapBackedConversationState.java) that shows
how the state can easily be stored using `HashMap`.

## Redux

I writing and using version `1` of this framework I realized that state management
quickly became non-trivial in larger applications. While updating Conversation Kit
for version `2`, I became inspired by a number of other projects
I was working on that were using [Redux](https://redux.js.org/) or a derivative for
state management. Redux is traditionally used in front end code to build graphical user
interfaces (GUI). At it's core it features a predictable, centralized container for application
state. While a *conversational user interface* is somewhat different from a GUI, it
can still benefit tremendously from Redux-style store.

### Redux in Java

Redux is primarily a JavaScript library and I could not find an implementation I liked
in Java. I suspect this is because Redux relies heavily on functional programming
concepts which were not as widely supported in Java when Redux was becoming popular. Java 8
has nice support for functional programming. Redux has a fairly small API, so I
wrote my [own implementation](src/main/java/com/synclab/conversationkit/redux/Redux.java) 
for this project. I may pull that out into a separate project at some point.

### Typed State

The implementation I wrote is fairly consistent with the JavaScript version. The main
difference is that I wanted to use a typed state instead of a JavaScript object (or Java HashMap) for the
external API. This just means that the `Store` constructor must take an additional
argument of a [Function](https://docs.oracle.com/javase/8/docs/api/java/util/function/Function.html) that
accepts a HashMap and returns the typed state.

### Application State

By default the Redux implementation only handles the conversation state. There is no
requirement for the rest of your application to interact with it. However, since it
is useful to have a centralized state, you can easily add additional state slices
and reducer functions to the store. See
[ConversationGraphTest](src/test/java/com/synclab/conversationkit/impl/ConversationGraphTest.java)
for a complete example on how to construct a typed state with multiple reducers.

## Nodes

A conversation node is a vertex on the directed conversation graph containing
content for the bot to present to the user. Each node has
zero or more outbound edges and zero or more inbound edges. The conversation
traverses the graph between nodes by looking at the user's intent and choosing
the first matching edge at each vertex.

Each node contains a conversation snippet represents a small bit of dialog in a conversation.
In the case of a chat bot, this might represent a block of text sent as one
message. The content is stored in the `metadata` field of the node as JSON. The
structure will be highly implementation dependent and is designed to be completely
flexible. For a voice assistent, the `metadata` might store the link to an audio file.
For a Facebook Messenger bot, one node might hold the JSON representing a button and
another some text.

Creating a node from the JSON is the responsibility of a `JsonNodeBuilder`.

```java
@FunctionalInterface
public interface JsonNodeBuilder<N extends IConversationNode> {
    public N nodeFromJson(Integer id, String type, JsonObject metadata) throws IOException;
}
```

In the case of a Dialog Tree, a 
[DialogTreeNodeBuilder](src/main/java/com/synclab/conversationkit/builder/DialogTreeNodeBuilder.java) takes JSON that looks something like
the following and creates a `DailogTreeNode` from it.

```json
{
    "id": "1",
    "type": "DialogTree",
    "label": "1",
    "metadata": {
        "message": ["Hello I'm a test bot.", "How are you feeling today?"]
    }
}
```

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
[DialogTreeTest](src/test/java/com/synclab/conversationkit/impl/DialogTreeTest.java).

### ConversationNode

A `ConversationNode` is a more general implementation of `IConversationNode`. Most
likely you will want to use or extend this for your node implementation. See 
[DirectedConversationEngineTest](src/test/java/com/synclab/conversationkit/impl/DirectedConversationEngineTest.java).

## Edges

A conversation edge is a directed connection between two nodes on the
conversation graph. Each edge has exactly one start node and one end node,
but a node frequently has multiple outbound edges. The conversation
implementation will look at each outbound edge from a node in sequence to decide which
edge to use to continue traversing the conversation graph.

```java
public interface IConversationEdge<S extends IConversationState> {
    public Integer getEndNodeId();
    public String getIntentId();
    public boolean validate(I intent, S state);
    public List<Object> getSideEffects(I intent, S state);
}
```

After the `DirectedConversationEngine` determines a user's intent from a message,
it evaluates the outbound edges for the current node to move the conversation to 
the next node. The engine iterates over each outbound edge to find the first to

 1. Match the intent's ID with `getIntentId()`
 2. Return `true` from `validate(I intent, S state)`;

Once a match is found, the engine dispatches the side effect actions to the 
internal Redux store from `getSideEffects(I intent, S state)`.

### Validation Function

Use the `validate` function for cases where there are multiple edges with the same
intent or the intent requires preconditions in the state. For example, consider an
agent that takes food orders. There could be an intent to order a burger (i.e. `ORDER_BURGER`). The node could
have two outbound edges with the intent `ORDER_BURGER`, one to handle ordering a cheeseburger
and the other to handle ordering a hamburger. In this case, both statements

 * "I would like a cheeseburger."
 * "Let me have a regular burger."

would match the `ORDER_BURGER` intent. But the `validate` function on each edge could
look at the intent slots to only return `true` for the type of burger it is looking for.

Another way of achieving the same result in this case would be to have two different
intents `ORDER_HAMBURGER` and `ORDER_CHEESEBURGER`. But slot filling is a pretty 
handy feature of most NLU engines and the `validate` function lets you apply logic to
the conversation flow based on slot values.

A second use case for `validate` is to make sure that a precondition in the state
is met before proceeding along an edge. For example, if there are no burgers ready yet,
the state could have a key `burgers_ready:false`. In this case all `ORDER_BURGER` edges
may want to return `false` for `validate` to direct the user to order something different.
 
### Side Effects

Side effects are another concept borrowed from Redux. Side effects represent any
actions that should be taken by the application as a result of matching an intent
and moving the conversation to the next node. Side effects are dispatched to the
Redux store and should be an instance of `Action`. A common side effect is to update
the state with the results of the previous intent. From the above example, the edge 
matching `ORDER_BURGER` might dispatch a `CHEESEBURGER_ORDER_RECEIVED` action. The reducer 
could then update the state for the user from `current_order:['fries','coke']` to
`current_order:['fries','coke','cheeseburger']`.

Any side effect that is an instance of `Action` is processed synchronously. In other
words the state will be updated before the engine proceeds to the next conversation
node. Conversation kit also supports asynchronous actions using the `CompletableFutureMiddleware`.
If the instance of the side effect is a `Future`, the action will be handled by the 
middleware and [processed asynchronously](https://redux.js.org/advanced/async-flow). This 
is useful for longer running tasks that don't necessarily need to finish to let the
conversation proceed.

## Natural Language Understanding / Intent Processing

The edges on the conversation graph are matched to the interpreted purpose (i.e. intent) of
the user's last message. Determining intent from an utterance is in the domain of Natural
Language Understanding (NLU). Conversation kit does not provide sophisticated NLU capabilities. Instead
it is designed to integrate with any external NLU service. To use any third party 
NLU service, extend `IntentDetector` and pass an instance of
the class to the `DirectedConversationEngine`.

### NLU Services

There are several well-known vendors who offer Natural Language Understanding as a service.

 * Microsoft Cognitive Services Language Understanding ([LUIS](https://www.luis.ai/home))
 * Amazon Web Services [Lex](https://aws.amazon.com/lex/)
 * Google [Dialogflow](https://dialogflow.com/)
 * Facebook's [wit.ai](https://wit.ai/)

### RegEx "NLU"

All of the commercial NLU systems use some type of advanced deep learning technology to
provide the language understanding. For testing and prototyping purposes, 
Conversation Kit includes a `RegexIntentDetector`. This is a very primitive NLU that
just relies on RegEx matching to determine intent. The `RegexIntentDetector` is not
intended for production use.

## Putting It All Together

For an example of a conversation graph with all nodes, edges, and side effects all loaded from a JSON file, see
[ConversationGraphTest](src/test/java/com/synclab/conversationkit/impl/ConversationGraphTest.java).

![Directed Conversation Test](/src/test/resources/directed_conversation.svg?raw=true)

If you have questions or suggestions, you can contact me
[on Twitter](https://www.twitter.com/tyreus).
