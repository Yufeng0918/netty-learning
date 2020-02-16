# @Netty Notes

## 1. Netty
#### Network Application Framework
- Feature
    + Design
    	+ Support various transport type
    	+ Event model
    	+ High customizable thread model
    + Performance
    	+ High throughput, lower latency
    	+ Less resource consumption
    	+ Minimized memory copy
    + Security
    	+ SSL/TLS Supoort
- Architecture
    + Core
        + Event Model
        + Universal Communication API
        + Zero-Copy-Capable Rich Byte Buffer
        + io.netty.buffer, io.netty.common, io.netty.resolver, io.netty.resolver.dns
    + Protocal Support
        + HTTP & WebSocket
        + SSL
        + Google Protobuf
        + Large File Transfer
        + io.netty.codec.http, io.netty.codec.http2, io.netty.codec.smtp, io.netty.codec.haproxy
    + Transport Service
        + Socket & Datagram
        + HTTP Tunnel
        + In-VM Pipe
        + io.netty.transport.epoll, io.netty.transport.kqueue
- Scope
    + used by 30000+ open source project
    + top project
        + database: cassandra
        + spark, hadoop
        + rocket mq
        + grpc, apache dubbo
#### Netty vs JDK NIO
- Do more
    + netty support protocal in application layer
    + netty resolve pack and unpack automatically
    + netty customization
    + netty handle exception
- Do better
    + bug fix: fix jdk issue faster and response better
    + enhancement
        + Netty's ByteBuff
        + Netty's FashThreadLocal
    + isolated network implementation
#### Netty vs Apache Mina vs Grizzly
- Mina
    + same developer
    + Alex from Apache Directory work with Trustin/Norman Maurer from Netty2
- Grizzly
    + less developer
    + less document
    + slow development, small community
#### History
- 2004.06: netty2
- 2008.10: netty3
- 2013.07: netty4
- 2015.11: netty5 dropping, not performance benefit from ForkJoinPool 
***


## 2. IO Model
#### Typical IO Model
- BIO
    + blocking IO before JDK1.4
    + blocking and sync
- NIO
    + new IO after JDK1.4
    + non-blocking and sync
- AIO
    + JDK1.7
    + non-blocking and async
#### Blocking vs Non Blocking
- blocking: wait till data is ready
- non blocking: return if data is not ready or buffer is full
#### Sync vs Aysnc
- sync: program read data by itself
- async: os read data and program call once data is ready
#### Netty Support
- BIO: OIO
- NIO
    + blocking will consuem resource, less thread compared to BIO
    + NIO have higher perfomance than BIO in high concurrency
    + mainly support, in linux
- AIO
    + AIO is in-mature in linux
    + NIO/AIO performance almost same in linux
    + Not support
#### Switch IO Model
- NioEventLoopGroup vs OioEventLoopGroup
- NioSocketChannel vs OioSocketChannel
- Channel switch by Factory, Releection and Generic type
```
return channelFactory(new ReflectiveChannelFactory<C>(
  ObjectUtil.checkNotNull(channelClass, "channelClass")
));
```


## 2. Bootstrap/ChannelInitializer/ChannelHandler
#### ServerBootstrap to startup EventLoop
- initial boss and worker event loop
- serverBoostrap
    + group boss and worker event loop
    + declare server socket channel type
    + initial childHandler
- closeFuture return when only channel is close

```
// Create eventLoop
EventLoopGroup bossGroup = new NioEventLoopGroup();
EventLoopGroup workerGroup = new NioEventLoopGroup();
try{
    ServerBootstrap serverBootstrap = new ServerBootstrap();
    serverBootstrap.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class).childHandler(new TestServerChannelInitializer());
    ChannelFuture future = serverBootstrap.bind(8899).sync();
    future.channel().closeFuture().sync();
}catch (Exception e){
    e.printStackTrace();
} finally {
    bossGroup.shutdownGracefully();
    workerGroup.shutdownGracefully();
}
```
#### ClientBootstrap to startup EventLoop
- initial worker event loop only
- Boostrap
    + worker event loop
    + declare socket channel type
    + initial handler for worker
```
EventLoopGroup worker = new NioEventLoopGroup();
try{
    Bootstrap bootstrap = new Bootstrap();
    bootstrap.group(worker).channel(NioSocketChannel.class).handler(new MsgClientChannelInitializer());

    ChannelFuture channelFuture = bootstrap.connect("localhost", 8899).sync();
    channelFuture.channel().closeFuture().sync();
} catch (Exception e){
    e.printStackTrace();
} finally {
    worker.shutdownGracefully();
}
```

#### ChannelInitializer to add handler into pipeline
```
public class TestServerChannelInitializer  extends ChannelInitializer<SocketChannel>{
    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        // get the pipeline to add handler
        ChannelPipeline pipeline = ch.pipeline();
        pipeline.addLast("httpServerCodec", new HttpServerCodec());
        pipeline.addLast("testHttpServerHandler", new TestServerChannelHandler());
    }
}
```
#### ChannelHandler to execute the task
```
public class TestServerChannelHandler extends SimpleChannelInboundHandler<HttpObject>{
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, HttpObject msg) throws Exception {

        System.out.println(msg.getClass());
        System.out.println(ctx.channel().remoteAddress());
        
        if (msg instanceof HttpRequest) {
            System.out.println("Execute ChannelRead0");
            System.out.println("Method: " + ((HttpRequest)msg).method());

            ByteBuf content = Unpooled.copiedBuffer("Hello World", CharsetUtil.UTF_8);
            FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, content);
            response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/plain");
            response.headers().set(HttpHeaderNames.CONTENT_LENGTH, content.readableBytes());
            ctx.writeAndFlush(response);
            ctx.channel().close();
        }
    }
}
```
***

## 3. Channel Handler
#### ChannelHandler life cycle
+ handlerAdded
+ channelRegistered
+ channelActive
+ ChannelRead0
+ channelReadComplete
+ channelInactive
+ channelUnregistered
+ handlerRemoved
- inactive/unregister is only executed when channel is closed
- close channel manually
```
ctx.channel().close();
```
#### Channel Group
```
private static ChannelGroup channelGroup = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
```
#### Logging Handler
```
bootstrap.group(boss, worker).channel(NioServerSocketChannel.class).handler(new LoggingHandler(LogLevel.DEBUG)).childHandler(new HeartBeatServerChannelInitializer());
```


#### WebSocket
- HTTP cons
    + HTTP is not persistent connection
    + Overhead may be large for HTTP header
    + Server push data to client, no real time from server to client
- WebSocket server and client handshake
- WebSocket server/client exchange data via without overhead
- Client send server HTTP request with ws parameter, client/Server upgrade protocal from HTTP to WebSocket
- WebSocket Frame
    + TextWebSocketFrame
    + PingWebSocketFrame (heartBeat)
    + PongWebSocketFrame (heartBeat)
    + BinaryWebSocketFrame
    + CloseWebSocketFrame
    + ContinouationWebSocketFrame
- WebSocketServerProtocolHandler declare the WebSocket URI: ws://localhost:8080/<context_path>

```
public class WebSocketChannelInitializer extends ChannelInitializer<SocketChannel> {

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {

        ChannelPipeline pipeline = ch.pipeline();

        pipeline.addLast(new HttpServerCodec());
        pipeline.addLast(new ChunkedWriteHandler());
        // Aggregate the HttpRequest segment to complete HttpRequest
        pipeline.addLast(new HttpObjectAggregator(8192));
        pipeline.addLast(new WebSocketServerProtocolHandler("/ws"));
        pipeline.addLast(new TextWebSocketFrameHandler());
    }
}

public class TextWebSocketFrameHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame msg) throws Exception {
        System.out.println("Received: " + msg.text());
        ctx.channel().writeAndFlush(new TextWebSocketFrame("Server Time: " + LocalDateTime.now()));
    }
}
```


## 4. RPC
#### Overview
- cross language and platform, performance of component's communication
- interface, entity declaration file
- generate program language file from declaration file
- client invoke server's method as in process call
- RPC msg communication
    + java serialization
    + self-define structure
    + XML/Json
    + RPC framework
#### Protocal Buffers
- define the data structure to generate source code
- support java, python, c++, ruby, c#
- development:
    + add protoc to path
    + add protoc-java library
- proto buff spec
    + package: proto buff namespace and default java package, mandatory
    + java_package: explicit declaration java package, optional
    + java_outer_classname: out class, other message is inner class of this class
    + datatype: bool, int32, float, double
    + annotation
        + required: mandatory, is forever
        + optional: may or may not set
        + repeated: nay number of times

```
syntax = "proto3";
package com.bp.netty.proto;
option optimize_for = SPEED;
option java_package = "com.bp.netty.proto";
option java_outer_classname = "Student";
option java_multiple_files = true;
```

- builder vs message
    + message is immutable
    + builder to construct the message by build()
- command
    protoc --java_out=<source_directory> <protobuffer_file>
    protoc --java_out=src/main/java src/protobuf/DataInfo.proto    
- netty integration
    - Initializer
        + add ProtobufVarint32FrameDecoder, ProtobufDecoder, ProtobufVarint32LengthFieldPrepender, ProtobufEncoder Hanlder
        + ProtobufDecoder accept the message class defaultInstance
    - Handler accept the type of messageType

```
//Initializer
pipeline.addLast(new ProtobufVarint32FrameDecoder());
pipeline.addLast(new ProtobufDecoder(PersonDataInfo.Person.getDefaultInstance()));
pipeline.addLast(new ProtobufVarint32LengthFieldPrepender());
pipeline.addLast(new ProtobufEncoder());
    
//Handler    
public class PersonTestServerHandler extends SimpleChannelInboundHandler<PersonDataInfo.Person>    
```
- recommended implementation
    - define the message [header + body]
        + header: message type enum, header is message type
        + type: data via "oneof", only one field is set one time

```
message MsgData {
    enum MsgType{
        PERSON = 0;
        CAT = 1;
        DOG = 2;
    }
    
    required MsgType msgType = 1;
    oneof MsgBody {
        Person person = 2;
        Cat cat = 3;
        Dog dog = 4;
    }
}
```

#### Thrift
- Overview
    - support C++, Java, Python, PHP, Erlang
    - define the interface Description Language
- IDL
    - datatype: byte, i16, i32, i64, double, string, list, set, map
    - service: interface
    - struts: compose of data
    - enum
    - exception
    - typedef: typedef i32 int
    - namespace
    - include: include "global.thrift"
- command
    - thrift --gen "thrift_data_file"
- server
    - set the server as Nonblocking
    - set transport, protocal and processor(from thrift service)
- client
    - set transport and protocal, protocal above transport
    - call the remote server call
````
// server
TNonblockingServerSocket socket = new TNonblockingServerSocket(8899);
THsHaServer.Args arg = new THsHaServer.Args(socket).minWorkerThreads(2).maxWorkerThreads(4);
PersonService.Processor<PersonServiceImpl> processor = new PersonService.Processor<>(new PersonServiceImpl());

arg.transportFactory(new TFramedTransport.Factory());
arg.protocolFactory(new TCompactProtocol.Factory());
arg.processorFactory(new TProcessorFactory(processor));

TServer server = new THsHaServer(arg);
server.serve();


// client
TTransport transport = new TFramedTransport(new TSocket("localhost", 8899), 600);
TProtocol protocol = new TCompactProtocol(transport);
PersonService.Client client = new PersonService.Client(protocol);

````
- Architecture

| Client  | Server  | 
|---|---|
| Application Code | Application Code  |
| Client  | Processor  |
| write/read  | write/read |
| TProtocol  | TProtocol |
| TTransport  | TTransport |

- Protocol
    + TBinaryProtocol
    + TCompactProtocol
    + TJSONProtocol
    + TDebugProtocol
- Transport
    + TSocket: blocking
    + TFramedTransport: frame as unit
    + TFileTransport: transport file
    + TMemoryTransport: byteArrayOutputStream
    + TZlibTransport
- Server
    + TSimpleServer: single thread
    + TThreadPoolServer: multi thread, blocking io, every request is thread
    + TNonblockingServer: multi thread, no-blocking io, based on TFramedTransport
    + THsHaServer: half-sync, half-async, based on TFramedTransport
#### GRPC
- Overview
    + base on protocol buff3
    + generate client and server stub
    + bi-directional streaming and integrated auth   
    + mode
        + unary RPC
        + client stream
        + server stream     
        + bi-direction stream
- library
    + 'io.grpc:grpc-netty:1.4.0',
    + 'io.grpc:grpc-protobuf:1.4.0',
    + 'io.grpc:grpc-stub:1.4.0'   
- config plugin
    + https://github.com/google/protobuf-gradle-plugin
    + generate java code
    + add code to source set
- proto file
    + gradle generateProto
    + execution
        - request/response
        - request/stream response 
        - request stream/response
        - request stream/response stream
````
service StudentService {

    rpc GetRealNameByUsername(MyRequest) returns (MyResponse) {}
    rpc GetStudentsByAge(StudentRequest) returns (stream StudentResponse) {}
    rpc GetStudentListByAges(stream StudentRequest) returns (StudentResponseList) {}
    rpc BiTalk(stream StreamRequest) returns (stream StreamResponse) {}
}


message MyRequest { string username = 1;}
message MyResponse { string realname = 1;}
message StudentRequest { int32 age = 1;}
message StudentResponse {
    string name = 1;
    int32 age = 2;
    string city = 3;
}
message StudentResponseList { repeated StudentResponse studentResponse = 1; }
message StreamRequest { string request_info = 1;}
message StreamResponse { string response_info = 1;}
````
- source code
    + server
    + request vs response
        - request is object, response is StreamObserver object
        - streamObserver response by onNext
        - streamObserver complete by onCompleted
    + request vs response stream
        - StreamObserver response multiple result by onNext
        - client get response as iterator
    + request stream vs response
        - stub(non-blocking) to trigger the remote call
        - service implementation define the request callback, onNext, onError and onCompleted and return the request
        - client define the how response callback 
        - client send the request via request.onNext() on onCompleted()
````
// server
this.server = ServerBuilder.forPort(8899).addService(new StudentServiceImpl()).build().start();
System.out.println("server start");
this.server.shutdown();


public void getRealNameByUsername(MyRequest request, StreamObserver<MyResponse> responseObserver) {
    System.out.println("accept: " + request.getUsername());
    responseObserver.onNext(MyResponse.newBuilder().setRealname("yufeng").build());
    responseObserver.onCompleted();
}


@Override
public void getStudentsByAge(StudentRequest studentRequest, StreamObserver<StudentResponse> responseStreamObserver) {
    responseStreamObserver.onNext(StudentResponse.newBuilder().setName("SG").build());
    responseStreamObserver.onNext(StudentResponse.newBuilder().setName("NY").build());
    responseStreamObserver.onNext(StudentResponse.newBuilder().setName("BJ").build());
    responseStreamObserver.onCompleted();
}


@Override
public StreamObserver<StudentRequest> getStudentListByAges(StreamObserver<StudentResponseList> responseListStreamObserver) {

    return new StreamObserver<StudentRequest>() {
        StudentResponseList.Builder studentResponseListBuilder = StudentResponseList.newBuilder();

        @Override
        public void onNext(StudentRequest value) { studentResponseListBuilder.addStudentResponse(StudentResponse.newBuilder().setName("SG" + value.getAge()).setAge(value.getAge())); }

        @Override
        public void onError(Throwable t) { System.out.println(t.getMessage());}

        @Override
        public void onCompleted() {
            StudentResponseList studentResponseList = studentResponseListBuilder.build();
            responseListStreamObserver.onNext(studentResponseList);
            responseListStreamObserver.onCompleted();
        }
    };
}

// client
class StudentStreamResponse implements StreamObserver<StudentResponseList> {

    @Override
    public void onNext(StudentResponseList value) { value.getStudentResponseList().forEach(i -> System.out.println(i.getName() + " " + i.getAge()));}

    @Override
    public void onError(Throwable t) { System.out.println(t.getMessage()); }

    @Override
    public void onCompleted() { System.out.println("Completed");}
}
StudentServiceGrpc.StudentServiceStub stub = StudentServiceGrpc.newStub(managedChannel);
StreamObserver<StudentRequest> studentRequestStreamObserver = stub.getStudentListByAges(new StudentStreamResponse());
studentRequestStreamObserver.onNext(StudentRequest.newBuilder().setAge(20).build());
studentRequestStreamObserver.onNext(StudentRequest.newBuilder().setAge(30).build());
studentRequestStreamObserver.onCompleted();




@Override
public StreamObserver<StreamRequest> biTalk(StreamObserver<StreamResponse> streamResponseStreamObserver)  {

    return new StreamObserver<StreamRequest>() {
        @Override
        public void onNext(StreamRequest value) { streamResponseStreamObserver.onNext(StreamResponse.newBuilder().setResponseInfo("response" + value.getRequestInfo()).build()); }

        @Override
        public void onError(Throwable t) {System.out.println(t.getMessage());}

        @Override
        public void onCompleted() { streamResponseStreamObserver.onCompleted();}
    };
}


StreamObserver<StreamRequest> streamRequestStreamObserver = stub.biTalk(new StreamObserver<StreamResponse>() {
    @Override
    public void onNext(StreamResponse value) { System.out.println(value.getResponseInfo());}

    @Override
    public void onError(Throwable t) {System.out.println(t.getMessage());}

    @Override
    public void onCompleted() {System.out.println("completed");}
});
streamRequestStreamObserver.onNext(StreamRequest.newBuilder().setRequestInfo("10").build());
streamRequestStreamObserver.onNext(StreamRequest.newBuilder().setRequestInfo("20").build());
streamRequestStreamObserver.onNext(StreamRequest.newBuilder().setRequestInfo("30").build());
streamRequestStreamObserver.onCompleted();
````

- shutdown hook
    + shutdown normally or by event
    + shutdown hook is initialized but not started
    + shutdown hook may have multiple thread and not time consuming task
    + event to trigger shutdown sequence   
    
    
    

## 5. IO
#### Category
- input/output
- bytes[inputstream|outputstream]/char[reader|writer]
- node stream/filter stream
#### Decorator
- extends object
- extends more responsibility
- component
    + component[inputstream]: abstract interface
    + concrete component[file inputstream]: core function
    + decorator[filter inputstream]: have a component reference and decorator interface [super class]
    + concrete decorator[buffered inputstream]: add on function
#### Buffer
- capacity: positive and never change
- limit: index of first element is not for read/write, less than capacity
- position: index of next element for read/write, less than limit = 0 =< mark =< position =< limit =< capacity
- flip(): set limit to position, set position to 0
- mark(): set mark = position, reset(): set position = mark
- clear(): set limit = capacity, position = 0
- rewind(): set position = 0
- slice(): new sliced buffer position, limit, capacity is independent but with common data         
- ReadonlyBuffer: original buffer data change relect on ReadonlyBuffer
#### DirectBuffer
- directBuffer vs HeapBuffer
    + heapbuffer: copy the data of java memory to system memory, system memory's data deal with IO
    + directbuffer: direct deal with system memory, zero copy from java process to system
- Outside Java: allocate memory out-of java process
- Inside Java: address point to memory
- MappedByteBuffur
    - map file or partial file to memory
    - change to buffer will reflect to file
- HeapBuffer is not direct copy to I/O device
    - GC may happen during copying
    - copying between buffers is fast, deal with I/O is slow
    - HeapBuffer copy to I/O will allocate temp DirectBuff
````
RandomAccessFile randomAccessFile = new RandomAccessFile("NioTest09.txt", "rw");
FileChannel fileChannel = randomAccessFile.getChannel();
MappedByteBuffer mappedByteBuffer = fileChannel.map(FileChannel.MapMode.READ_WRITE, 0, 5);
````    
- Scattering && Gattering
    - scattering: socket read the data into buffer array
    - gattering: socket write buffer array data out
````
ByteBuffer[] buffers = new ByteBuffer[3];
buffers[0] = ByteBuffer.allocate(2);
buffers[1] = ByteBuffer.allocate(3);
buffers[2] = ByteBuffer.allocate(4);

SocketChannel socketChannel = serverSocketChannel.accept();
long r = socketChannel.read(buffers);
long r = socketChannel.write(buffers);

```    
#### Selector    
- A multiplexor of SelectableChannel objects, created System's default selector provider via open()  to create a new selector. open till close() invoke.
- SelectableChannel register with a selector by SelectionKey
- Selection Operation
    + The key set
        - keys() 
        - all keys representing registered selectable channels, channel register to selector
        - key will added into key set as registration side effect
    + The selected-key
        - selectedKeys()
        - each key's channel was detected to be ready for at least one of the operations.
        - key added into selected-key set in selection  operation
    + The cancelled-key
        - keys that have been cancelled but whose channels have not yet been deregistered
        - key will added cancelled key set and channel will deregister in next selection operation
    + selection operation
        - select(), selectNow(), select(long)
        - key in cancelled-key will remove from each key set and channel is deregistered
        - os queried for an update as to the readiness of remaining channel to perform any of the operations identified by its key's interest set. For a channel that is ready for at least one such operation
            + not in the selected-key set, then added to selected-key set and update its ready-operation set is modified for which the channel is now reported to be ready.
            + in the selected-key set, so its ready-operation set is modified to identify any new operations for which the channel is reported to be ready.
- SelectionKey
    - interest set determines which operation categories will be tested for readiness the next time one of the selector's selection methods is invoked.         
    - ready set identifies the operation categories for which the key's channel has been detected to be ready by the key's selector.
```
while (true) {
    selector.select();
    Set<SelectionKey> selectionKeys = selector.selectedKeys();
    Iterator<SelectionKey> iterator = selectionKeys.iterator();
    while (iterator.hasNext()) {

        SelectionKey selectionKey = iterator.next();
        if (selectionKey.isAcceptable()) {
            ServerSocketChannel serverChannel = (ServerSocketChannel) selectionKey.channel();
            SocketChannel socketChannel = serverChannel.accept();
            socketChannel.configureBlocking(false);
            socketChannel.register(selector, SelectionKey.OP_READ);
            iterator.remove();
            System.out.println("connection from " + socketChannel);
        } else if (selectionKey.isReadable()) {
            SocketChannel socketChannel = (SocketChannel) selectionKey.channel();
            int byteRead = 0;
            while(true) {
                ByteBuffer buffer = ByteBuffer.allocate(256);
                buffer.clear();
                int read = socketChannel.read(buffer);
                if(read <= 0) {
                    break;
                }
                buffer.flip();
                socketChannel.write(buffer);
                byteRead += read;
            }
            System.out.println("read: " + byteRead + ", from " + socketChannel);
            iterator.remove();
        }
    }
}
```


## 6. Charset
#### Overview
- encode: CharBuffer to ByteBuffer
- decode: ByteBuffer to CharBuffer
- file save into disk as bytes.
#### Encoding Standard
- ASCII: American Standard Code for Information Interchange, 7 bit for 1 char, total is 128 char
- ISO-8859-1: 8 bit(1 byte ) for 1 char, total is 256 char, compatible for ASCII 
- GB2312: 2 bytes as Chinese Char
- BIG5: Taiwan
- Unicode: for every thing, 2 bytes as 1 char
- UTF: Unicode Translation Format, unicode is encoding standard, UTF is storage standard, implementation of Unicode
#### UTF
- UTF-16
    + zero width No-Break Space, 0xFEFF, 0xFFFE
    + UTF-16LE (little ending): start with 0xFFFE
    + UTF-16BE (big ending): start with 0xFEFF
- UTF-8
    + variable length storage
    + 3 bytes for 1 chinese char
- BOM (Byte Order Mark)
#### Sample
- source utf-8, destination utf-8, encoding/decoding via utf-8 is ok, intermediate is good
- source utf-8, destination utf-8, encoding/decoding via iso-8899-1 is ok, intermediate is disorder
- source utf-8, destination gbk, encoding/decoding via iso-8899-1 is bad, intermediate is disorder



## 7. Zero Copy
#### Copy
- User space read() syscall Kernel Space
- Kernel Space ask data from Hardware
- Kernel Space copy the data into User Space
- User space copy data to target buffer for socket
- User space write() syscall to Kernel Space
- Kernel Space write data into socket
- Hardware notify Kernel Space is done
- Kernel Space notify User space is done
#### Zero Copy
- User space sendfile() syscall Kernel space
- Kernel space ask data from Hardware into kernal buffer
- Kernel space write file descriptor into target buffer
- Kernal space write the file to Hardware via kernal buffer[data] + target buffer[descriptor]
- Kernel space notify sendfile() to User space           



## 8. Source Code
#### EvenLoopGroup
- extends EventExecutorGroup
- allow registering channel for later selection
- interface:
    + next(): return next EventLoop
    + register(Channel)/register(ChannelPromise): register channel
- NioEventGroup initial array of EventExecutor[number of processor * 2]
- EventExecutor is generic EventExecutorGroup to see if thread is execute in a event loop
- ThreadPerTaskExecutor decouple the initialization  of thread and how task is executed   
    + constructor: ThreadFactory with priority, name, daemon status
    + execute: ThreadFactory -> newThread(Runnable r).start()  
    + Executor: decoupling task sumission from mechanics of how each task will be run (thread's detail, scheduling)
- Structure
    + SingleThreadEventLoop
        + SingleThreadEventExecutor
            + AbstractEventExecutor
                + EventExecutor(Int)
                    + EventExecutorGroup(Int)
                        + AbstractExecutorService
                            + ExecutorService (Int)
        + EventLoop (Int)
            + EventLoopGroup (Int)
                + EventExecutorGroup(Int)
- SelectorProvider
	+ step1: java.nio.channels.spi.SelectorProvider to get provider name
	+ step2: ServiceLoader to get provider name
	+ setp3: sun.nio.ch.DefaultSelectorProvider.create() choosed by JDK
#### Bootstrap
- group
    + set the parent(acceptor) and child(client), handle all the event for ServerChannel and Channel
    + Parent to be set in AbstractBootstrap
        + register parent to serverSocketChannel
    + ChildGroup to be set in ServerBootstrap
        + register childGroup to SocketChannel
        + register Channel via EventExecutorChooser
        	+ GenericEventExecutorChooser: incr 1 mod length
        	+ PowerOfTwoEventExecutorChooser: incr 1 & (length-1). e.g & '111'

```
AbstractBootstrap: ChannelFuture initAndRegister() {
    channel = channelFactory.newChannel();
    ChannelFuture regFuture = config().group().register(channel);
}

ServerBootstrap: void channelRead(ChannelHandlerContext ctx, Object msg) {
    inal Channel child = (Channel) msg;
    childGroup.register(child).addListener(new ChannelFutureListener());
}

```
- channel(Class)
    + set the channelFactory via ReflectiveChannelFacotory
    + channel is not setup yet 
    + NioServerSocketChannel use NIO selector based implementation to accept new connection
- childHandler
    + set childHandler to serve the request
- ChannelFuture
    + Future(JDK)
        + asynchronous computation, provided to check if the computation is complete, to wait for its completion, and to retrieve the result of the computation.
        + cancel(Boolean), isCancelled(), isDone(), get()
        + get() is blocking
    + Future(Netty)
        + addListenser: listening the future result, notify the listener to execute operationComplete() when task is complete
        + Listener implements GenericFutureListener, GenericFutureListener has operationComplete(Future) method to pass future to trigger operation after future complete
            + operationComplete verify future.isDone or success to trigger operation
            + operationComplete as callback method
        + removeListenser
        + isSuccess: I/O operation is completed successfully
    + ChannelFuture
        + All I/O operations in Netty are asynchronous. It means any I/O calls will return immediately with no guarantee that the requested I/O operation has been completed at the end of the call
        + status
            + Uncompleted: isDone() =false, isSucess()=false, isCancelled()flase
            + Completed
                + success: isDone() = true, isSuccess() =  true
                + failure: isDone() = true, cause not null
                + cancel: isDone() = true, isCancelled() = true
        + recommend use listener to performa operation after I/O is done
        + Do not call await() inside ChannelHandler
            - ChannelHandler are usually called by an I/O thread. If await() is called by an event handler method, which is called by the I/O thread, the I/O operation it is waiting for might never complete because await() can block the I/O operation it is waiting for, which is a dead lock.
            - Bad Example: channelRead0() { future.await() }
            - Good Example: chann elRead0() { future.addListenser(ChannelFutureListener)}
        + Do not confuse I/O timeout and await timeout
            - await timeout does not means I/O timeout, I/O may not complete yet
            - set I/O timeout options I/O, I/O timeout means future is failed
            - Bad Example: ChannelFuture f = b.connect(...); f.awaitUninterruptibly(10, TimeUnit.SECONDS);
            - Good Example: b.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10000); ChannelFuture f = b.connect(...); f.awaitUninterruptibly();
- Promise
    + setSuccess: only one time 
    + setFailure: only one time
    + ChannelPromise
        + notify the listeners to via set success or set failure to trigger operation complete
- ChannelPipeline
    - implementation of intercepting Filter, list of channelHandler
    - full control over how inbound and outbound event is handled
    - pipeline is created automatically when a channel is created
    - pipeline contains ChannelHandlerContext, ChannelHandlerContext contains ChannelHandler
    - In: socket read, then Inbound Handler from 1 to N
    - Out: Outbound Handler from 1 to N, then socket write
    - E.g. Pipeline add 1 in, 2, in, 3 out, 4 out, 5 InAndOut
        + in: 1, 2, 5
        + out: 5, 4, 3
    - Create in AbstractChannel
        + reference of channel
        + create tail as TailContext and head as HeadContext
    - Forwarding event to next handler via event propagation method
    - Building Pipeline
        - solution 1
            - pipeline.addLast(group, "handler", new Handler())
            - time consuming task execute by EventExecutorGroup
        - solution 2
            - handler contains thread pool
    - Thread safety
        - handler can be add/remove any time
- Channel
    - nexus to network socket which is capable of I/O operation
    - provide
        + current state
        + configuration parameters
        + support I/O operation
        + channelPipeline to handle all I/O event
    - all I/O operations are asynchronous
    - channel are hierarchical
        + depend on how it was created, socketChannel parent is ServerSocketChannel
    - downcast to access transport-specific operation
    - release resource
        + close() or close(ChannelPromise)
- SimpleChannelInboundHandler
    - extends from ChannelHandlerAdapter
    - rewrite the channelRead0, channelRead to release message by ReferenceCountUtil
- bind(InetAddress)
    - validate group and child
    - init(Channel)
        - Channel has ChannelConfig and Attribute(AttributeMap)
        - After 4.1, Channel and ChannelHandlerContext Attr has same scope
        - set channelOptions
            + ChannelOption allow to config ChannelConfig in safety way
            + T is type of ChannelOption
            + extends from AbstractConstant implments Constants
            + ChannelOptions only contains Id/name but not value, ChannelOptions is constants created from Constants Pool
            + ChannelConfig
                + map key value, Map<ChannelOption, Object>
        - set AttributeKey
            + extends from AbstractConstant
            + AttributeMap<AttributeKey<T>, Attribute<T>>
        - create NioServerSocketChannel via channelFactory
            + extends from AbstractChannel, AbstractChannel init the channelPipeline
            + selectable channel from java.nio
                + set the channel config
                    + initial AdaptiveRecvByteBufAllocator, start with 64, increase 16[512], multiple 2 to out of memory
                    + AdaptiveRecvByteBufAllocator.HandleImpl to return next buffur size
                    + ByteBuffAllocator to allocate memory
                        + Android use heap buffer
                        + io.netty.nounsafe use heap buff
                        + sun.mics.unsafe to allocate direct buff
            + set the blocking as false
            + set interest accept operation
        - set channel options and attribute
        - get pipeline to add ChannelInitializer, this is default ChannelInitializer for ServerSocketChannel
            + add ChannelInitializer to last of pipeline
            + ChannelInitializer extends from ChannelInboundHandlerAdapter
            + ChannelInitializer setup pipeline and ChannelInitializer is a ChannelHandler
                + ChannelHandlerContext
                    + Enables a ChannelHandler to interact with its ChannelPipeline and other handlers.
                    + Modify the pipeline, A handler can modify the ChannelPipeline it belongs to dynamically.
                    + A handler can have more than one context, a handler instance can be added into more than one ChannelPipeline, then more than one ChannelHandlerContext
                + generate default name for ChannelHandler
                + Create ChannelHandlerContext, bridge between ChannelPipeline and ChannelHandler
                + addLast to add handler between tail.prev and tail
                + trigger ChannelHandler's handlerAdd method
            + Channel is not registered yet
                + add PendingHandlerAddedTask(ctx) to trigger handlerAdd method later
        - add ServerBootstrapAcceptor to ServerSocketChannel to accept the connection
    - register
        - config: return ServerBootstrapConfig
        - group: return NioEventLoopGroup extends MultithreadEventLoopGroup
        - register: EventExecutorChooser use round-robin to choose next EventExecutor
            + Chooser is create from DefaultEventExecutorChooserFactory
                + PowerOfTwoEventExecutorChooser and GenericEventExecutorChooser use round robin
            + SingleThreadEventLoop: execute all its submitted task in single thread
                + use DefaultChannelPromise to register
                + verify is current thread is SingleThreadEventLoop
                + register SelectableChannel to Selector
#### Thread Model
- One EventLoopGroup contains one or more than one EventLoop
- One EventLoop is binding to one Thread
- All EventLoop deal with all I/O event with binding Thread
- One Channel is registered in one EventLoop
- One EventLoop is assigned to one or more than one Channel
- Channel is thread-safe, Channel create a task and let it executed in EventLoop
- Time-consuming task  should not in Handlers
    + JDK: get ExecutorServices in handlers
    + Netty: pipeline pass EventExecutor
   


## 9. Reactor
#### Network Service
- Structure
    - Read Request
    - Decode request
    - Process service
    - Encode reply
    - Send reply
- Classic Service
   - Client send request
   - each handle of thread to read, decode, compute, encode, send
   - while (true) { new Thread(new Handler(ss.accept())).start() }
- Divide and Conquer
    - process into small task without blocking
    - non-blocking read/write
    - dispatch IO event
- Reactor
    - reactor responds to IO event by dispatching handler
    - handler perform non-blocing action
    - binding handlers to event
    - implementation I
        + Overview
            + Reactor init to interst OP_ACCEPT
            + OP_ACCEPT Key bind to Acceptor
            + Acceptor accept connection and create Handler
            + Handler interest OP_READ
            + Selector.selector for loop the SelectionKey
        + Reactor
            + Constructor()
                + open selector
                + register server channel to selector to get SelectionKey
                + selectionKey for op_accept to attach to Acceptor(selector)
            + Run()
                + selecotor.select(); while (Iterator(selectionKeySet)) { dispatch((selectionKey)it.next())}
                + dispatch() { selectionKey.attachment().run()} 
                + this selectionKey.attachement() is Acceptor or Handler
        + Acceptor
            + Runnable for severSocketChannel to accept()
            + call new Handler(selector, socketChannel)
        + Handler
            + Constructor()
                + register selector with OP_READ
                + selectionKey attach this Handler
            + Run()
                + handle read/write request
    - Components:
        + handle (SelectionKey)
            + provided by system for event description
            + event from outside, e.g. connection, data. 
            + event from inside, e.g. schedule task
        + Synchronous Event Demultiplexer
            + Selector
            + wait for event， select(), poll()
        + EventHandler
            + callback for particular event
        + ConcreteEventHandler
            + implementation of EventHandler
            + bussiness logic in callback
        + Initialization Dispatcher 
            + Reactor, provide event registration
            + dispatch handler to handle the event
    - Flow
        + Channel register to Initiation Dispatcher with event which is linked by Handle
        + Initiation Dispatcher ask event's Handle when event happen
        + After registeration, handle_vent will bootstrap Initiation Dispatcher, use Demultiplexer to wait for event happen
        + when event's Handle is ready, Demultiplexer will notify Initiation Dispatcher
        + Initiation Dispatcher execute Event Handler's callback for event's Handle with particular type
    - Worker Threads
        - handler will slow down reactor
        - offload non-IO processing to other thread
        - thread pool to control the handler   
    - Multi-Reactor
        - multi selector
        - main reactor link to acceptor, interest on OP_CONNECT
        - sub reactor link to handler, interest on OP_READ
- Dispatcher
    - Server
        + Acceptor register handler() to Dispatcher
        + Dispatcher init and select()
        + client connnect to dispatcher
        + Dispatcher call Acceptor handle_event()
        + Acceptor accept and create Handler
        + Handler register handler() to Dispatcher
    - BossGroup and WorkerGroup have different selector
               
               

## 10. Points
#### Channel vs ChannelContext
- channel method go through every handler
- channelContext method will go from current handler, short circuit
#### Server and Client as one
- server and client use same event loop



## 11. ByteBuf
#### Categrory
- HeapBuf
    + Good: store data in JVM heap, direct access byte[]
    + Bad: I/O will copy data to directBuf
- DirectBuf
    + allow memory out of JVM.
    + Good: high performance for socket IO
    + Bad: allocate/release memory is more complicated than HeapBuf, no direct access byte[]  
    + Notes
        + Netty deal with BufPool
        + Business logic in HeapBuf, I/O in DirectBuf
- CompositeBuf
#### Creation of a buffer
- Unpooled rather than calling an individual implementation's constructor.  
#### Index accessing
- Random access: index
- Sequential access: readerIndex and writeIndex
    + 0 < readerIndex < writerIndex < capacity
    + o - readerIndex: discardable bytes
    + readerIndex - writerIndex: readable bytes
    + writable - capacity: writable bytes    
    + readerIndex move only reading: isReable() to verify if more buff to read
    + writerIndex move only writing: maxWritableBytes() >= 4[base on data type]
#### Operation
- discardReadBytes(): move the readerIndex and writerIndex
- clear(): reset readerIndex and writeIndex 
- duplicate(): shadow copy, a derived buffer have independent readerIndex and writeIndex. But refer to same copy if data.
- copy(): deep copy, copy data and index
- toString(Charset): to convert to string
#### NettyBuf vs JDK Buf
- Netty buff seperate via readerIndex and writexIndex
- Netty buff throw IndexOutOfBoundsException if readerIndex greater than writerIndex
- Netty buff maintain readerIndex or writeIndex for I/O
- Netty buff default size is Integer.MAX_VALUE
- JKD buff bytes[] is final, unable to increase/decrease size after allocation
- JDK buff only use one position for index, read/write via flip()  


## 12. ReferenceCounted
#### Overview
- retain() + 1 and release() -1
- referencedCounted container of other object implement referenceCounted
- 0 will be deallocated 
- for loop to use AtomicIntegerFieldUpdater.compareAndSet
    + object which field currently handle
    + only update if expect value = current value
    + update success will break the loop
    + only guarantee atomically only with respect to other invocations of compareAndSet
- only one static AtomicIntegerFieldUpdater for all AbstractReferenceCounted object
- last method set the ref to zero to release buff memory space
- derived buff will not change referenceCount. e.g. buff from duplicated()
    + derived buff should invoke retain() method if need to use buffer to prevent orginal buff to release
```
private static final AtomicIntegerFieldUpdater<AbstractReferenceCounted> refCntUpdater = AtomicIntegerFieldUpdater.newUpdater(AbstractReferenceCounted.class, "refCnt");
for (;;) {
    int refCnt = this.refCnt;
    if (refCnt < decrement) {
        throw new IllegalReferenceCountException(refCnt, -decrement);
    }
    if (refCntUpdater.compareAndSet(this, refCnt, refCnt - decrement)) {
        if (refCnt == decrement) {
            deallocate();
            return true;
        }
        return false;
    }
}
```
#### AtomicIntegerFieldUpdater
- must be int data type
- must be volatile
    + visible by thread
- must be non static
- only visible for scope, not for others, e.g reflection
#### Inbound && Outbound
- Inbound messages
    + ChannelHandler release buffer, try {read} finally {buff.release()} 
- Outbound messages      
    + outbound message created by application
    + write should release the buff
 
 
 
## 13. Handlers
#### Overview
- ChannelInboundHandler and ChannelOutboundHandler
- Encoding/Decoding all are handlers
- Encoding: char to bytes[], ChannelOutboundHandler
- Decoding: bytes[] to char, ChannelInboudnHandler
- Encoder & Decoder must match the type for encoding/decoding, otherwise this encoder or decorder will not be executed
- Decoder need to verify the length of readable bytes
#### ByteToMessageDecorder
- convert to ByteBuf 
- Frame detection
    - ensure enough bytes in buffer from complete frame by checking readableBytes()
    - not sharable
#### ReplayingDecoder
- extends from ByteToMessageDecorder
- allow you to implement the decode() and decodeLast() method just like all required bytes were received already
- wrapper ReplayDecorderBuf, throw error is not enough data in buffer
- rewind the readerIndex and call decode method again if more data received in buffer
- limitation
    + some buffer operation are prohibited
    + performance maybe worse if network is slow
- improve performance
    + use checkpoint to record status
```
switch (state()) {
   case READ_LENGTH:
     length = buf.readInt();
     checkpoint(MyDecoderState.READ_CONTENT);
   case READ_CONTENT:
     ByteBuf frame = buf.readBytes(length);
     checkpoint(MyDecoderState.READ_LENGTH);
     out.add(frame);
     break;
   default:
     throw new Error("Shouldn't reach here.");
}
```   
#### LengthFieldBasedFrameDecoder
- splits ByteBuf dynamically by value of length field. Header with integer represent the length of message body
- proprietary client-server protocals.
- property
    + lengthFieldOffset
    + lengthFieldLength
    + lengthAdjustment: -2 to include the 2 bytes header
    + initialBytesToStrip



## TCP Encode && Decode
- TCP is stream protocal
- no boudary in message

#### Sticky Packet
- receive more than one packet at same time
- response data is less than buffer, packet two message to delivery

#### Half packet
- response data is greater than buffer
- MTU maximum Transmission Unit, exceed MTU must be unpack

#### Solution
- Fix Length
	+ easy to implment but not efficient
	+ waste space
	+ FixLengthFrameDecoder to decode
		+ framelength
- Delimiter
	+ easy to implment but not efficient
	+ special character required parse
	+ DelimiterBasedFrameDecoder to decode
		+ support multiple delimiters
- Fix Length + Data
	+ length field to read, body is data
	+ max length is predicate, recommended
	+ LengthFieldBasedFrameDecoder to decode
		+ lengthFieldOffset: offset in length field
		+ lengthFieldLength: length of length field
		+ lengthAdjustment: info between length field and data
		+ initialBytesToStrip: can be used to strip the length field
	+ LengthFieldPrepender to encode

#### ByteToMessageDecoder
- convert stream data to user data
- channelRead: read data save via cumulator
	+ Cumulator
		+ MERGE_CUMULATOR: memory copy, default cumulator
		+ COMPOSITE_CUMULATOR: add component to List<Component>
- callDecoe: all FrameDecorder extend ByteToMessageDecoder to implement decode()

#### MessageToMessageDecoder & MessageToMessageEncoder
- convert user data to java object
- consideration factor
	+ space usage
	+ speed
	+ human readable
	+ language support
- Typical Multiple encoding & decoding
	+ json
	+ protobuf
	+ xml
	+ base64

***


## KeepLive
#### Overview
- communication point crash
- communication unreachable
- communication too busy to handle
- connection broken
#### TCP Keepalive in Transport Layer
- no data communication after 7500, send 9 probe message, interval 75 = 2 hours 11 min
- OS TCP parameter
	+ net.ipv4.tcp_keepalive_time = 7200
	+ net.ipv4.tcp_keepalive_intvl = 75
	+ net.ipv4.tcp_keepalive_probes = 9
#### Keepalive in Application Layer
- release idle connection, client need to reconnect to server 
- IdleStateHandler
    + readerIdleTime: timeout for read
    + writerIdleTime: timeout for write
    + allIdleTime: timeout for read/write
- IdleStateEvent is triggered by read/write/all timeout event, sequent Handler handle timeout event by type 

```
public class HeartBeatServerChannelInitializer extends ChannelInitializer<SocketChannel> {
    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();
        pipeline.addLast(new IdleStateHandler(5, 7, 10, TimeUnit.SECONDS));
        pipeline.addLast(new HeartBeatServerChannelHandler());
    }
}

public class HeartBeatServerChannelHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (! (evt instanceof IdleStateEvent))
            return;
        IdleStateEvent event = (IdleStateEvent) evt;
        System.out.println(ctx.channel().remoteAddress() + " Timeout: " + eventType);
        ctx.channel().close();
    }
}
```
***


## Lock
#### Synchronization
- atomic: execute all or none
- visible: visible change to others
- orderable: execution sequence
#### Lock Category
- Permisitic, Optimitic
- Fair, Unfaire
- Shared, Exclusive
#### Netty Concurrency
- Synchronized block: minimum the sync scope
- Volatile primary type + Static AtomicFieldUpdater
	+ save space compared to AtomicLong
	+ volatile long = 8 bytes
	+ AtomicLong = 32 bytes
- LongAdder: compare to AtomicLong in high concurrency after JDK8
- MpscChunkedArrayQueue
	+ LinkedBlockingQueue: multi producer, mutli consumer
	+ MpscChunkedArrayQueue: multi producer, single consumer
- ThreadLocal
#### Applicated Scenario
- overall parallel + part serialize = one queue + multithread
- avoid the context switch
***


## Memory Optimization
#### Objective
- consume less memory, reduce GC
- run faster

#### Principal
- use primary type, not wrapper type
- use static variable, not member variable
- use object pool, e.g. Apache Commons Pool, io.netty.util.Recycler
- Zero-Copy
	+ compositeByteBuf: use List<Compoennt> instead of copying
	+ unpooled.wrappedBuffer(bytes): wrap instead of copying
	+ transferTo(): jdk function, zero copy
	+ off heap: avoid GC and copy

#### ByteBuf
- UnpooledByteBuf: default for android
- PooledByteBuf: default for other platform
	+ get(): recycler pop buf from stack
	+ deallocate(): recycler put buf to stack
- ByteBuffer.allocateDirect(initCapacity) to allocate the direct buffer
***


## Event Bootstrap
#### Main thread
- create selector
- create serverSocketChannel
- init serverSocketChannel
- assign NioEventLoop to serverSocketChannel from bossGroup

#### BossGroup
- register serverSocketChannel to NioEventLoop's selector
- bind address
- accept OP_ACCEPT to selector

#### Technical Sequence
- NioEventLoopGroup
	+ Both for bossGroup and workerGroup
	+ [Multiple|Single]EventExecutorGroup: MultipleEventExecutorGroup() to init EventExecutor[n]
	+ NioEventLoop: implements EventExecutor to initial selector
- ServerBootstrap
	+ bind the server to port
	+ AbstractBootstrap inovke initAndRegister()
		+ Initialization 
			+ ReflectiveChannelFactory init channel as NioServerSocketChannel
			+ create channel's pipeline and add ChannelInitializer
				+ ChannelInitializer's tasks
					+ add handler channel's pipeline, e.g LoggingHandler
					+ add ServerBootstrapAcceptor to channel's pipeline
			+ remove ChannelInitializer from channel's pipeline
			+ ChannelPipeline(LoggingHandler, ServerBootstrapAcceptor)
		+ Registration
			+ SingleThreadEventLoop put register task and put into NioEventLoop
			+ SingleThreadEventExecutor start thread for NioEventLoopGroup			+ NioEventLoopGroup
				+ register NioEventLoopGroup'selector to NioServerSocketChannel
				+ interest ops is OP_READ
			+ DefaultChannelPipeline
				+ DefaultChannelPipeline bind NioServerSocketChannel to port
			+ FireChannelActive
				+ fireChannelActive event to AbstractNioChannel to register OP_ACCPT to selector

```
# 1. NioEventLoop: open Selector
final SelectorTuple selectorTuple = openSelector();

# 2. NioServerSocketChannel: instance by ReflectiveChannelFactory(NioServerSocketChannel.class)
channel = channelFactory.newChannel();

# 3. NioServerSocketChannel[bossGroup]: Register OP_READ to NioServerSocketChannel
selectionKey = javaChannel().register(eventLoop().unwrappedSelector(), 0, this);

# 4. DefaultChannelPipeline:  Bind address to NioServerSocketChannel
javaChannel().bind(localAddress, config.getBacklog());

# 5. Pipeline‘fireChannelActiveEvent: readInterestOp is 16
selectionKey.interestOps(interestOps | readInterestOp);
```


## Event OP_ACCEPT
#### BossGroup
- NioEventLoop poll OP_ACCEPT from selector
- create socketChannel
- assign NioEventLoop from workerGroup

#### WorkerGroup
- register socketChannel to NioEventLoop's selector
- register OP_READ to selector

#### Technical Sequence
- NioEventLoop
	+ invoke run() to listen OP_ACCEPT event
	+ invoke processSelectedKey() to process selectedKey
- NioServerSocketChannel
	+ invoke doReadMessage to accept OP_ACCT to create SocketChannel
- Pipeline
	+ fireChannelRead event
		+ ServerBootstrap register workerGroup NioSocketChannel in channelRead()
		+ AbstractNioChannel register workGroup's Selector to SelectionKey
	+ fireChannelReadComplete event
		+ add OP_READ into selectionKey	

```
# 1. NioEventLoop: run loop to select key
selector.select();
processSelectedKey();
NioServerSocketChannel.read()

# 2. NioServerSocketChannel: create SocketChannel
SocketChannel ch = SocketUtils.accept(javaChannel());

# 3. Pipeline‘fireChannelReadEvent[workerGroup]: ServerBootstrap to register childGroup 
childGroup.register(child).addListener(new ChannelFutureListener())
selectionKey = javaChannel().register(eventLoop().unwrappedSelector(), 0, this);

# 4. Pipeline‘fireChannelReadCompleteEvent[workerGroup]: NioSocketChannel add OP_READ into selectionKey
selectionKey.interestOps(interestOps | readInterestOp);

```
***


## Event OP_READ (Read)
#### Read efficiently
- AdaptiveRecByteBufAllocator
	+ guess buffer for next time, default size is 1024
	+ SIZE_TABLE	
		+ [16, 32, 48.. 512], increase 16 for i++, decrease for consective 2 times unfull reading
		+ [512, 1024, 2048...], increae till overflow, increase if full reading current time 
- DefaultMaxMessagePerRead: consective read data

#### Write efficiently
- Method
	- write: ChannelOutboundBuffer.addMessage
	- flush
		+ ChannelOutboundBuffer.addFlush
		+ NioSocketChannel.doWrite
	- writeAndFlush
- Beware
	+ register OP_WRITE and invoke later if currently unwriteable, OP_WRITE means writable
	+ adjust maxBytesPerGatheringWirte if flush all last time
	+ continue write till unwriteable or flush 16 times (writeSpinCount)
	+ writeBufferWaterMark.high() indicate data waiting to write exceed threshold

#### Technical Sequence
- allocate 1024 byte buff
- read data from Channel into byteBuff
- record received data size to precidate size for next time
- trigger pipeline.fireChannelRead(byteBuf)
	+ DefaultChannelPipeline.HeadContext invoke channelRead
		+ find the next Handler able to execute channelRead()
	+ inbound
		+ Head, Context[Handler], Context[Handler], Tail
		+ Handler implments ChannelInboundHandler
		+ channelRead() does not have @Skip
	+ outbound
		+ Tail, Context[Handler], Context[Handler], Head
		+ ChannelOutboundBuffer.addMessage to write message to List
		+ ChannelOutboundBuffer.addFlush to point flushedEntry to unflushedEntry
		+ NioSocketChannel.doWrite to write data to channel
		+ ChannelHandlerContext.channel().write: write from TailContext
		+ ChannelHandlerContext.write: write from current Context
- verify if byteBuf is full
	+ true: consective read till 16 times
	+ false: wait for next OP_READ

```
1. NioEventLoop to read the data
NioSocketChannel.read()


2. ReadBuf into List<Object> till nothing left or 16 ByteBufs
do {
    int localRead = doReadMessages(readBuf);
} while (allocHandle.continueReading());


3. Fire ChannelRead event for each ByteBufs
Pipeline.fireChannelRead()[*]

4. Fire ChannelReadComplete event after all ByteBuf sent
Pipeline.fireChannelReadComplte()

```

## Event OP_READ (Close)
#### Technical Sequence
- AbstractNioByteChannel
	+ release ByteBuf
	+ set ChannelOutboundBuffer to null
- AbstractSelectableChannel
	+ selectionKey.cancel() to remove key from Selector
	+ AbstractNioChannel remove key from Selector incase direct invoke deregister()
- Pipeline
	+ fireChannelInactiveAndDeregister event
	+ fireChannelUnregister event
#### Status
- normal to return 1
- abnormal to throw IO Exception

```
1. Channel close and release tyepBuf
AbstractInterruptibleChannel.close()
release(byteBuf)

2. Cancel selectionKey
selectionKey.cancel()
```
***


## Event Shutdown
#### Technical Sequence
- [bossGroup|workerGroup].shutdownGracefully()
- verify State is close [Begin Loop]
	+ close all channel
		+ bossGroup close NioServerSocketChannel
		+ workerGroup close NioSocketChannel
	+ execute task/hooks
		+ verify if exceed gracefulShutdownTimeout
			+ exit application if exceed timeout
			+ continue to check gracefulShutdownQuietPeriod
		+ verify if exceed gracefulShutdownQuietPeriod
			+ exit application if exceed timeout
			+ if task/hooks executed in quiet period to start loop again

```
1. NioEventLoop to close channel
ch.unsafe().close() in loop

2. Execute closeTask/Hook
- selector.close()

3. confiremShutdown
compare lastExecutionTime to gracefulShutdownQuietPeriod

```


## Network Application Go Live
#### Transport Protocol
- Define data structure and encoding/decoding
	+ data encoding/decoding
	+ data compression
	+ sticky pack and half pack
#### Client/Server
	+ client
	+ server
#### Code Review
- Best Pratice in Netty
- Known issue in Netty
- Learn from other project using Netty
- Diagnose & Measurement
#### Online Feedback
- error collection
- performance collection
***


## Online Order Project
#### Operation
- Auth
	+ AuthOperation
	+ AuthOperationResult
- Order
	+ OrderOperation
	+ OrderOperationResult
- Keeplive
	+ KeepliveOperation
	+ KeepliveOperationResult
#### Data Structure
- MessasgeHeader: length, version, opCode, streamId
	+ opCode: indicate different operation
- MessageBody: JSON
