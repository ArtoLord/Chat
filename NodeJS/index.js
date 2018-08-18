const Koa = require('koa'); // core

const cors = require('@koa/cors');
const Router = require('koa-router'); // routing
const bodyParser = require('koa-bodyparser'); // POST parser
const serve = require('koa-static'); // serves static files like index.html
const logger = require('koa-logger'); // optional module for logging
const uuid = require('uuid/v1');
const passport = require('koa-passport'); //passport for Koa
const LocalStrategy = require('passport-local'); //local Auth Strategy
const JwtStrategy = require('passport-jwt').Strategy; // Auth via JWT
const ExtractJwt = require('passport-jwt').ExtractJwt; // Auth via JWT

const jwtsecret = "mysecretkey"; // signing key for JWT
const jwt = require('jsonwebtoken'); // auth via JWT for hhtp
const socketioJwt = require('socketio-jwt'); // auth via JWT for socket.io

const mongoose = require('mongoose'); // standard module for  MongoDB
const crypto = require('crypto'); // crypto module for node.js for e.g. creating hashes
const koaBody = require('koa-body')({multipart:true});

const app = new Koa();
const router = new Router();
app.use(serve('public'));
app.use(logger());
app.use(cors())
app.use(bodyParser());

app.use(passport.initialize()); // initialize passport first
app.use(router.routes()); // then routes
const server = app.listen(3000);// launch server on port  3000

mongoose.Promise = Promise; // Ask Mongoose to use standard Promises
mongoose.set('debug', true);  // Ask Mongoose to log DB request to console
mongoose.connect('mongodb://localhost'); // Connect to local database
mongoose.connection.on('error', console.error);

//---------User Schema------------------//

const userSchema = new mongoose.Schema({
  email: {
    type: String,
    required: 'e-mail is required',
    unique: 'this e-mail already exist'
  },
  passwordHash: String,
  salt: String,
  socketioID: String,
  roomlist:Array,
  last_online:Date,
  online:Boolean,
  avatar:String,
}, {
  timestamps: true
});
const messageSchema = new mongoose.Schema({
  from:String,
  to:String,
  text:String,
  date:Date,
  data:String,
}, {
  timestamps: true
});

const roomSchema = new mongoose.Schema({
  name: String,
  type: String,
  inside:Array,
  avatar:String
}, {
  timestamps: true
});

const imageSchema = new mongoose.Schema({
  name: String
}, {
  timestamps: true
});




//----to User-------//


var toUser =  function(user, Jwt){
	user = {_id : user._id, email : user.email, jwt:Jwt }
	return user;
	}
	
//----Models use ------//

userSchema.virtual('password')
.set(function (password) {
  this._plainPassword = password;
  if (password) {
    this.salt = crypto.randomBytes(128).toString('base64');
    this.passwordHash = crypto.pbkdf2Sync(password, this.salt, 1, 128, 'sha1');
  } else {
    this.salt = undefined;
    this.passwordHash = undefined;
  }
})

.get(function () {
  return this._plainPassword;
});

userSchema.methods.checkPassword = function (password) {
  if (!password) return false;
  if (!this.passwordHash) return false;
  return crypto.pbkdf2Sync(password, this.salt, 1, 128, 'sha1') == this.passwordHash;
};

const User = mongoose.model('User', userSchema);
const RoomList = mongoose.model('RoomList',roomSchema);
const Messages = mongoose.model('Messages',messageSchema);
const Images = mongoose.model('Images',imageSchema);


//----------Passport Local Strategy--------------//

passport.use(new LocalStrategy({
    usernameField: 'email',
    passwordField: 'password',
    session: false
  },
  function (email, password, done) {
    User.findOne({email}, (err, user) => {
      if (err) {
        return done(err);
      }

      if (!user || !user.checkPassword(password)) {
        return done(null, false, {message: 'User does not exist or wrong password.'});
      }
      return done(null, user);
    });
  }
  )
);

//----------Passport JWT Strategy--------//

// Expect JWT in the http header

const jwtOptions = {
  jwtFromRequest: ExtractJwt.fromAuthHeader(),
  secretOrKey: jwtsecret
};

passport.use(new JwtStrategy(jwtOptions, function (payload, done) {
    User.findById(payload.id, (err, user) => {
      if (err) {
        return done(err)
      }
      if (user) {
        done(null, user)
      } else {
        done(null, false)
      }
    })
  })
);

 


//------------Routing---------------//

// new user route
router.post('/user',async(ctx, next)=> {
  try {
    ctx.body = await toUser(User.create(ctx.request.body),null);
  }
  catch (err) {
    ctx.status = 400;
    ctx.body = err;
  }
});

// local auth route. Creates JWT is successful

router.post('/login', async(ctx, next) => {
  await passport.authenticate('local', function (err, user) {
    if (user == false) {
		console.log(ctx.request.body);
      ctx.status = 404;
      
    } else {
		console.log(user._id);
      //--payload - info to put in the JWT
      const payload = {
        id: user.id,
        email: user.email
      };
      const token = jwt.sign(payload, jwtsecret); //JWT is created here

      ctx.body = toUser(user,'JWT ' + token);
      console.log(ctx.body);
    }
  })(ctx, next);

});

// JWT auth route

router.get('/custom', async(ctx, next) => {

  await passport.authenticate('jwt', function (err, user) {
    if (user) {
      ctx.body = "True"
    } else {
      ctx.status = 400;
      console.log("err", err)
    }
  } )(ctx, next)

});

router.get('/getroomlist',async(ctx,next)=>{
	await passport.authenticate('jwt', async(err, user)=>{
		
    if (user) {		
		ctx.body = [];
			for(var i=0;i<user.roomlist.length;i++){
				await RoomList.findOne({name:user.roomlist[i]},(err,room)=>{
					console.log(room.avatar);
					ctx.body.push([room.name,room.avatar]);
					});
				}
     
    }
  })(ctx,next);
});


router.get('/getallroom',async(ctx,next)=>{
	await passport.authenticate('jwt', async(err, user)=>{
		
    if (user) {		
		
			await RoomList.find({},{}, async(err,rooms)=>{
				ctx.body = [];
				for (var j=0; j<rooms.length;j++){
					if (rooms[j].type=="open")
					await ctx.body.push([rooms[j].name,rooms[j].avatar]);
					}
				})
     
    }
    
  })(ctx,next);
});

router.post('/set_room_avatar',async(ctx,next)=>{
    var room = ctx.request.body.room;
    var id = ctx.request.body.id;
    await RoomList.update({name:room},{avatar:id},async(err,room)=>{});
});

router.post('/set_user_avatar',async(ctx,next)=>{
    var room = ctx.request.body.room;
    var id = ctx.request.body.id;
    await User.update({email:room},{avatar:id},async(err,room)=>{});
});
	

router.post('/getmessages',async(ctx,next)=>{
	
	await passport.authenticate('jwt',async(err, user)=>{
		var a = [];
		console.log(ctx.request.body.date);
			await Messages.find({to:ctx.request.body.to,date:{$gt:new Date(ctx.request.body.date)}},async(err,msg)=>{
				for (var j=0; j<msg.length;j++){
					await a.push({text:msg[j].text,to:msg[j].to,from:msg[j].from,id:msg[j]._id,date:msg[j].date.getTime(),data:msg[j].data});
					}
					ctx.body = a;
				
				});
			
     
     
  })(ctx,next);
});



router.post('/getusers',async(ctx,next)=>{
	var re = new RegExp(ctx.request.body.to,'i');
	await User.find({email:re},async(err,user)=>{
		console.log(user);
		var a= [];
		for (var j=0; j<user.length;j++){
			var b = [];
			b.push(user[j].email);
			a.push(b);
			};
		ctx.body = a;
		ctx.status = 200;
		next;
		});
});





	

//---Socket Communication-----//
var fs = require('fs');
var GridFS = require('gridfs');
var Mongo = require('mongodb');
var express = require('express');
var app1 = express();
app1.use(express.static("C:\\NodeJS\\Pictures\\"));
var http = require('http').Server(app1);
var io = require('socket.io')(http);



const formidable = require('formidable');
http.listen(8080);


app1.post('/upload',function(req,res){
	var form = new formidable.IncomingForm();
	form.parse(req,function(err,fields,files){
		console.log(files);
		fs.readFile(files.picture.path,'binary',function(err,data){
			
			Images.create({name:files.picture.name},function(err,file){
				fs.open("\Pictures\\"+file._id,'w',function (err,file){
				fs.close(file,function(err,file1){});
				});
			fs.writeFile("\Pictures\\"+file._id,data,'binary',function (err,file){});
				res.send(file._id);});
			});
		});
	});
	
	app1.get('/imageload',function(req,res){
		id = req.query.id;
		if (id!="Hello" && id!="null"){
	console.log(id = req.query.id);
	Images.findOne({_id:id},function(err,file){
		res.sendFile(process.cwd()+"\\Pictures\\"+file._id);})
		};
	});


io.on('connection', function(socket){
  socket.on('disconnect', function(){
	  
	 var d = new Date();
	  User.update({SocketioID:socket.id},{last_online:d.getTime()},(err,user)=>{})
	  
    console.log('user disconnected');
  });
  //socket.on('chat message', function(msg){
    //socket.broadcast.emit('chat message', msg,"all","all");
  //});
  
  socket.on('conn', function(email){ 
	  
      User.update({"email":email},{"socketioID":socket.id},(err, user) => {});
    
  });
  socket.on('send to', function(msg,room,type,data){
	  console.log("send to");
	  if (type =="user"){
		  User.findOne({"email":room}, (err, user) => {
      if (err) {
		  console.log(err);
        return;
      }
      a = user.roomlist;
      var sqrt;
      User.findOne({socketioID:socket.id},(err, user) => {
		  var d = new Date();
		  Messages.create({text:msg,to:room,from:user.email,date:d,data},(err,mess)=>{
			  console.log(d.getTime());
		  io.to(room).emit('chat message', msg,user.email,room,"user",mess._id,d.getTime(),data)
		  });});
    });
		  }
	  else{                          //type = room
		 
					  
					  
			  
		   User.findOne({socketioID:socket.id},(err, user) => {
			   var d = new Date();
      Messages.create({text:msg,to:room,from:user.email,date:d,data},(err,mess)=>{
			  RoomList.findOne({name:room},(err,rm)=>{
				  for(var i = 0; i<rm.inside.length;i++){
					  User.findOne({email:rm.inside[i]},(err,usr)=>{
						  io.to(usr.socketioID).emit('chat message', msg,user.email,room,"room",mess._id,d.getTime(),data);
						  });
		  
		  };});});});
      
		  };});
  
  socket.on('new room', function(msg,type){
	  RoomList.findOne({name:msg}, (err, room) => {
		  User.findOne({socketioID:socket.id},(err,user1)=>{
      if (err) {
		  console.log(err);
        return;
      }
      else if(room!=null){
		  
		  io.to(socket.id).emit('chat message', "Эта комната уже есть","Server",user1.email,"Server");
		  return;
		  }
		  else{
			  
			  a = [];
			  a.push(user1.email);
      

		RoomList.create({'name':msg,'type':type,inside:a},(err,user)=>{});
		  socket.join(msg);
		  User.findOne({socketioID:socket.id},(err,user)=>{
			  a = user.roomlist;
			  a.push(msg);
		  User.update({socketioID:socket.id},{'roomlist':a},(err,user1)=>{});
		  var d = new Date();
		  Messages.create({text: user.email+" conected",to:msg,from:"Server",date:d},(err,mess)=>{
			  
		  RoomList.findOne({name:msg},(err,rm)=>{
						  io.to(socket.id).emit('chat message',user.email+" conected" ,"Server",msg,"room",mess._id,d.getTime(),null);
		  
		  });
		  });});}});
    
		});
    
  });
  socket.on('to the room', function(msg){
	  User.findOne({socketioID:socket.id},(err,user)=>{
	RoomList.findOne({name:msg}, (err, room) => {
		var bol = false;
      user.roomlist.forEach(function(item,i,arr){
		  if (item==room.name){
			  bol = true;
			  }
		  });
      if (err) {
		  console.log(err);
        return;
      }
      
      else if(room==null||bol){
		  
		  io.to(socket.id).emit('chat message', "Нет такой комнаты или вы уже в ней","Server",user.email,"Server");
		  
		  }
		  else{
			  a = room.inside;
			  a.push(user.email);
			  RoomList.update({name:msg},{inside:a},(err,rm)=>{});
			   socket.join(msg);
			  a = user.roomlist;
			  a.push(msg);
		  User.update({socketioID:socket.id},{'roomlist':a},(err,user1)=>{});
		  var d = new Date();
		  Messages.create({text: user.email+" conected",to:msg,from:"Server",date:d},(err,mess)=>{
		  RoomList.findOne({name:msg},(err,rm)=>{
				  for(var i = 0; i<rm.inside.length;i++){
					  User.findOne({email:rm.inside[i]},(err,usr)=>{
						  if(usr.socketioID!=null){
						  io.to(usr.socketioID).emit('chat message',user.email+" conected" ,"Server",msg,"room",mess._id,d.getTime(),null);}
						  });
		  
		  };});
		  });
			  }
			  });
		 });
  });
  
  
  socket.on('leave room', function(msg){
	  User.findOne({socketioID:socket.id},(err,user)=>{
	  RoomList.findOne({name:msg}, (err, room) => {
      if (err) {
		  console.log(err);
        return;
      }
      
      else if(room==null){
		  
		  io.to(socket.id).emit('chat message', "Нет такой комнаты или вас в ней нет","Server",user.email,"Server");
	  
		  
		  }
		  else{
			   
		   User.findOne({socketioID:socket.id},(err,user)=>{
			  a = []
			  user.roomlist.forEach(function(item,i,arr){
				  if (!(item==msg)){
					  a.push(item);
					  }
				  })
			  var d = new Date();
		  User.update({socketioID:socket.id},{roomlist:a},(err,user1)=>{});
		  Messages.create({text:user.email+" leaved",to:msg,from:"Server",date:d},(err,mess)=>{
		  RoomList.findOne({name:msg},(err,rm)=>{
			  var a = [];
			  for (var i = 0;i<rm.inside.length;i++){
				  if(rm.inside[i]!=user.email){
					  a.push(user.email);
					  }
				  }
				  for(var i = 0; i<rm.inside.length;i++){
					  User.findOne({email:rm.inside[i]},(err,usr)=>{
						  if(usr.socketioID!=null){
						  io.to(usr.socketioID).emit('chat message',user.email+" leaved" ,"Server",msg,"room",mess._id,d.getTime(),null);}
						  });
					  };
		  
		  });
		  });});
		}});});
  });
  
  socket.on('inwite user', function(user_email,msg){
	   User.findOne({email:user_email},(err,user)=>{
	RoomList.findOne({name:msg}, (err, room) => {
		var bol = false;
      user.roomlist.forEach(function(item,i,arr){
		  if (item==room.name){
			  bol = true;
			  }
		  });
      if (err) {
		  console.log(err);
        return;
      }
      
      else if(room==null||bol){
		  
		  io.to(socket.id).emit('chat message', "Нет такой комнаты или пользователь уже в ней","Server",user.email,"Server");
	  
		  
		  }
		  else{
			  a = room.inside;
			  a.push(user.email);
			  RoomList.update({name:msg},{inside:a},(err,rm)=>{});
			  socket.join(msg);
			  a = user.roomlist;
			  a.push(msg);
		  User.update({email:user_email},{'roomlist':a},(err,user1)=>{});
		  var d = new Date();
		  Messages.create({text: user.email+" был приглашен",to:msg,from:"Server",date:d},(err,mess)=>{
		  RoomList.findOne({name:msg},(err,rm)=>{
				  for(var i = 0; i<rm.inside.length;i++){
					  User.findOne({email:rm.inside[i]},(err,usr)=>{
						  io.to(usr.socketioID).emit('chat message',user.email+" был приглашен" ,"Server",msg,"room",mess._id,d.getTime(),null);
						  });
		  
		  };});
		  });
			  }
		  });
		  });
    
  });
  
  });
  
  
  
