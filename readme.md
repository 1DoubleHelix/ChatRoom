Java基于TCP实现多人聊天室，可以私聊或者创建群聊，JSwing实现GUI

# 主要功能

1. 用户注册、登录、好友关系保存
2. 跟好友一对一聊天
3. 文件发送 组建聊天室群聊

## 客户端（Client）：

1. 当用户点击注册按钮，弹出新窗口，有用户名和密码需要输入，在客户端需要确认密码大于等于四位。格式正确后将用户名和密码合并，中间用空格分开，然后将该String传给服务器，再在while循环中等待服务器输入："
   FAILEDTOREGISTER"或者"SUCCESSFULLYREGISTER"，分别为注册失败和注册成功，客户端有相应提示；

2. 点击登录按钮，将用户名和密码同样是用空格分开传给服务器，服务器查询数据库，客户端等待服务器返回，若为FAILEDTOLOGIN，则提示用户登录失败；若为SUCCESSFULLYLOGIN，提示用户登录成功，打开新窗口；

3. 新窗口中有添加好友，私聊和群聊三个页面。

4. 点击添加好友，弹出新窗口，要求输入好友用户名，然后服务器返回该用户名是否存在，若存在则询问用户是否添加好友，若确定，则向被添加好友发送请求，若好友已经添加，则收到服务器发来的"FRIENDALREADYEXISTS"
   ，若用户名不存在，则收到"USERNOTFOUND"，若用户名存在且未添加，则收到"SUCCESSFULLYADDTHEFRIEND"，客户端均有相应提示；

5. 当客户端点击"私聊选项卡"，向服务端发送"SHOWFRIENDS"，然后收到服务器返回的字符串"FRIENDSLIST friendname1 friendname2 friendname3"
   ，客户端需要将各个好友名字分割，每个好友在用户列表中各占一行，每一行显示好友名字，且拥有一个"私聊"按钮，点击该按钮弹出私聊窗口。点击发送按钮，向服务器发送"PRIVATEMESSAGE 接收方名字 message"。若收到"
   PRIVATEMESSAGE 发送方名字 message"，则在窗口中添加一行："friendname : message"；

6. 聊天框除了消息框和输入框还附加了文件发送按钮，点击文件发送按钮可以选择文件，同时在聊天框显示传输文件名，好友客户端则把文件存放在默认路径下；

7.

用户点击组建聊天室按钮，则弹出新的窗口，窗口中有输入框提示输入群聊ID号，有组建新群聊和加入群聊两个按钮，用户输入ID号，点击组建群聊按钮（传送CREATEGROUP和群聊ID拼接在一起的字符串（此处中间不要用空格分开！）给服务器）或者加入群聊按钮（传送LEAVEGROUP和群聊ID拼接在一起的字符串（此处中间不要用空格分开！）给服务器），客户端判断群聊ID大于等于4才允许将此ID发送给服务器，服务器返回组建成功或者加入成功，或者组建失败（群聊ID号已存在），或者加入失败（群聊ID号不存在）的信息，客户端提示用户；

8. 在输入框中输入群聊ID，点击创建群聊按钮，将"CREATEGROUP ID"发送至服务器，若创建成功返回"CREATEGROUPSUCCESSFULLY"，否则返回"GROUPALREADYEXISTS"
   。点击加入群聊按钮，若加入成功则会收到JOINGROUPSUCCESSFULLY，并且弹出群聊聊天框，左上角显示群聊ID号，否则收到GROUPNOTEXISTS，弹窗提示用户群聊不存在。在聊天框，点击发送按钮，发送GROUPMESSAGE
   ID MESSAGE给服务器若收到\"GROUPMESSAGE ID name message，表示群友昵称为name的用户正在说话，对应的群聊聊天框中显示该内容。若点击聊天框右上角交叉，则关闭该窗口，并且向服务器发送"
   LEAVEGROUP ID"；

9. 在while循环中，若收到服务器的命令未符合以上所有选项，则在控制台输出服务器命令。

## 服务器（Server）：

一个线程负责处理一个客户端的数据，即有唯一用户名name

在运行开始时：

in = new Scanner(socket.getInputStream());

out = new PrintWriter(socket.getOutputStream(), true);

### 类（class）：

1. 群聊Group类：每个群聊group拥有一个元素为PrintWriter类的writers集合（Set\<PrintWriter\> writers = new HashSet\<\>();）用以在该群内广播。

函数

(1) 构造函数：接收一个String类型参数即ID号，赋值给其内部变量this.ID

(2) 为群聊添加成员的函数addMember：接收两个参数一个是该成员的用户名，另一个是该用户对应的PrintWriter writer，并将writer"同步"添加到writers中，随后群聊人数记录的count++。

(3) 为群聊删除成员的函数deleteMember：接收该用户对应的PrintWriter writer，将writer从writers中删除，随后群聊人数记录的count\--。

(4) groupChat函数：利用writers进行群聊

2. 群聊Groups类：字典groups，键为群聊ID，值为group类型，

函数

(1)createGroup，接收一个String类型参数，前缀是"CREATEGROUP"，11位之后是该群聊ID，调用groupExist判断是否已存在重复组ID，若存在则传送"FRIENDALREADYEXISTS"
给客户端，并return，否则调用Group类构造函数，创建新的群聊，并调用该群聊的addMember，将当前用户添加至群聊中，随后将该新创建的group添加到groups字典中，添加成功，传送"
SUCCESSFULLYCREATEGROUP"给客户端

(2)静态函数groupExist：接收String类型参数ID，查询groups字典返回是否存在该组（此函数在run函数中调用，根据返回结果决定创建新的组还是将该成员添加到组中）

3. ChatServer类（维持整个服务器数据，最大的类，其他类为其内部类）：

(1) 用户名密码users：用字典存储，用户名为键，密码为值，都是String类型

(2)好友关系relationship：用字典存储，用户名为键，好友用户名集合为值（HashSet）

(3)群聊groups：大集合groups里面的元素是小集合group，小集合对应着一个群聊，小集合元素是String，即各个用户名

(4)全部人的PrintWriter：allWriters，为集合，键为用户名，值为printwriter，即当前在线人数

函数

(1) 构造函数：初始化groups等变量

4. Handler类（维持某个线程，即某个socket数据）

变量：private String name;

private Socket socket;

private Scanner in;

private PrintWriter out;

Private set\<String\> friends;

函数

(1) 构造函数：this.socket = socket;

(2) createUser()
，接收一个字符串参数，该参数为账号和密码，用空格分开，分割该字符串后，将同步users字典，将判断其是否已经存在于字典中，若不存在，则添加进字典中，this.name=name,并返回true，否则返回false。

(3) allowLogin()，接收一个字符串参数，该参数为账号和密码，用空格分开，分割该字符串后，判断其是否存在用户该用户且密码正确，若存在且正确返回true，否则返回false。

\(4\) loginOrRegister(),在while(true)循环体内，out.println(\"LOGINORREGISTER\")
;，要求客户端返回登录或者注册命令，如果返回注册命令，调用createUser方法，并返回是否注册成功给用户（"FAILEDTOREGISTER"和"SUCCESSFULLYREGISTER"
），并continue；如果返回登录命令，调用allowLogin()方法，若返回false则返回"FAILEDTOLOGIN"表明不存在用户或者密码错误，continue，否则返回"SUCCESSFULLYLOGIN"
登录成功，break。

\(5\) loadFriend()，依据relationship加载该用户所有的好友至friends中

\(6\) requestFriend()方法，接收String类型参数，取其13位之后的值作为待添加好友名字，好友，查找该用户名，若该用户已经存在于friends中，则传递"FRIENDALREADYEXISTS"
给客户端，若该用户不存在，则传递"USERNOTFOUND"给客户端，若找到该用户，将该用户名添加到friends中，且同步更新relationship，传递"SUCCESSFULLYADDTHEFRIEND"给客户端

\(7\) listFriend，返回本用户所有好友用户名给客户端，名字以空格作为分割

\(8\) privateChat函数：接收字符串，后几位为好友用户名，查找对应friendWriter是否存在，若不存在，即好友不在线，传递"FRIENDNOTONLINE"给客户端,否则while循环内： String input =
in.nextLine(); //持续接收来自客户端输入

//先判断friendWriter是否为空，若为空，return

friendWriter.println(\"MESSAGE \" + name + \": \" + input); //广播该消息

}

\(9\) run()方法：

try:

将writer同步添加到AllWriter中

运行loginOrRegister，直至登陆成功

while(true):

判断收到的命令是什么：

若前缀是"REQUESTFRIEND"，后缀是好友用户名，则调用requestFriend方法；continue

若前缀是"CREATEGROUP"，后缀是组ID，调用goups.createGroup函数

紧接着调用groupChat函数

若前缀是"JOINGROUP"，后缀是组ID，查找groups的对应group，调用其对应的addMember，

紧接着调用groupChat函数

若前缀是"LEAVEGROUP"，后缀是组ID，查找groups的对应group，调用其对应的deleteMember，并判断其count是否为0，若为0则remove该groups中的group

若前缀是"PRIVATECHAT"，后缀是好友用户名，则调用privateChat函数

若前缀是"ENDPRIVATECHAT"，后缀是好友用户名，则调用privateChat函数

Finally:

if (out != null) {

allWriters.remove(out);

}

socket.close()
