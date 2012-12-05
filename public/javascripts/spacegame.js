var ships = {};
var bullets = [];
var me;
var ws;

function SpaceGame(ctx, name, gameUrl)
{
  me = new Ship(name);
  var liElement = document.getElementById(name);
  liElement.style.color = me.color;
  this.ctx = ctx;
  //this.ctx = document.getElementById('gameCanv').getContext('2d');
  ships[name] = me;

  ws = new WebSocket(gameUrl);

  ws.onopen = function()
  {
    ws.send(me.genMoveString());
  };

  ws.onmessage = function(e) 
  { 
    //console.log(e.data);
    var msgType = 'move';
    if (msgType === e.data.substring(0, msgType.length))
      onMoveRecv(e.data, ships);

    msgType = 'bullet';
    if (msgType === e.data.substring(0, msgType.length))
      onBulletRecv(e.data);

    msgType = 'join';
    if (msgType === e.data.substring(0, msgType.length))
      onJoin(e.data);
  };

  //start animation
  this.draw = function()
  {
    this.ctx.clearRect(0,0, gameCanv.width, gameCanv.height);
    for (s in ships)
      ships[s].draw();
    var stillThere = [];
    for (b in bullets)
    {
      if (!bullets[b].thrust)
        continue;
      bullets[b].draw();
      stillThere.push(bullets[b]);
    }
    bullets = stillThere.splice(0);
  
  }
  setInterval(this.draw, 10);

  //controls
  pageBody.onkeypress = function(e)
  {
    // left rotate
    if (e.charCode == "z".charCodeAt(0))
      me.rot -= .5;
    // right rotate
    if (e.charCode == "x".charCodeAt(0))
      me.rot += .5;
    //move forward
    if (e.charCode == ".".charCodeAt(0))
      me.moveForward(25);
    //fire bullet
    if (e.charCode == "/".charCodeAt(0))
    {
      var b = new Bullet(me);
      bullets.push(b);
      ws.send(b.toWSString());
    }
    ws.send(me.genMoveString());
  };
}


function Ship(name) 
{
  this.name = name;
  this.x = Math.floor(gameCanv.width  * Math.random());
  this.y = Math.floor(gameCanv.height * Math.random());
  this.rot = -Math.PI / 2;
  this.width = 16;
  this.height = 30;
  this.thrust = 0;
  this.color = getRandomColor();

  this.draw = function()
  {
    ctx.save();
    this.update(1);
    //generate xform matrix
    ctx.translate(this.x, this.y);
    ctx.rotate(this.rot);

    ctx.fillStyle = this.color;
    //draw a triangle
    ctx.rotate(Math.PI / 2);
    ctx.beginPath();
    ctx.moveTo(-this.width / 2, this.height / 2);
    ctx.lineTo( this.width / 2, this.height / 2);
    ctx.lineTo(             0 ,-this.height / 2);
    ctx.fill();

    ctx.restore();
  };

  this.moveForward = function(px) { this.thrust += px; };

  this.update = function(dt)
  {
    //convert rads -> norm
    //multiply force "vector" by forward thrust
    //Apply force to absolute position
    this.x += Math.cos(this.rot) * this.thrust;
    this.y += Math.sin(this.rot) * this.thrust;
    //Need to make less drag. ?maybe /= smthing
    this.thrust /= 2;
    //last step, after all forces applied wrap x and y
    this.x %= gameCanv.width;
    this.y %= gameCanv.height;
    if (this.x < 0)
      this.x += gameCanv.width;
    if (this.y < 0)
      this.y += gameCanv.height;
    this.checkHit();
  };

  this.checkHit = function ()
  {
    var savedBullets = [];
    for (var i in bullets)
    {
      if (this.name != bullets[i].name)
      {
        //check for collision
        //check dist between ship and bullet < ship.width
        var dx = this.x - bullets[i].x;
        var dy = this.y - bullets[i].y;
        var distSq = dx * dx + dy * dy;
        if (distSq < (this.width * this.width))
        {
          //handle collision
          if (this.name == me.name)
          {
            //announce hit
            //ws.send('newHit: ' + me.name);
            me.x = Math.floor(gameCanv.width  * Math.random());
            me.y = Math.floor(gameCanv.height * Math.random());
            me.rot = -Math.PI / 2;
            ws.send(this.genMoveString());
            alert('you got hit');
          }
        }
        else
          savedBullets.push(bullets[i]);
      }
      else
        savedBullets.push(bullets[i]);
    }
    bullets = savedBullets.slice(0);
  };

  this.genMoveString = function()
  {
    var data = [];
    data.push('newMove: ' + this.name);
    data.push('x: '       + this.x);
    data.push('y: '       + this.y);
    data.push('rot: '     + this.rot);
    data.push('thrust: '  + this.thrust);
    data.push('color: '   + this.color);
    return data.join('&');
  };
}



function Bullet(ship)
{
  this.x = ship.x;
  this.y = ship.y;
  this.name = ship.name
  this.rot = ship.rot;
  this.thrust = 200;
  this.color = ship.color;

  this.draw = function()
  {
    if (!this.thrust)
      return;
    this.update(1);
    ctx.save();

    ctx.fillStyle = this.color;
    ctx.translate(this.x, this.y);

    ctx.beginPath();
    ctx.arc(0, 0, 10, 0, Math.PI * 2, true); 
    ctx.closePath();
    ctx.fill();

    ctx.restore();
  };

  this.update = function(dt)
  {
    var dist = dt * 1;
    dist = dist > this.thrust ? this.thrust : dist; 
    this.thrust -= dist;
    this.x += Math.cos(this.rot) * dist;
    this.y += Math.sin(this.rot) * dist;

    this.x %= gameCanv.width;
    this.y %= gameCanv.height;
    this.x = this.x < 0 ? this.x + gameCanv.width : this.x;
    this.y = this.y < 0 ? this.y + gameCanv.height : this.y;
  };

  this.toWSString = function()
  {
    var data = [];
    data.push('newBullet: ' + this.name);
    data.push('x: '         + this.x);
    data.push('y: '         + this.y);
    data.push('rot: '       + this.rot);
    //data.push('thrust: '    + this.thrust);
    return data.join('&');
  };
}


function onMoveRecv(data, ships)
{
  var pairs = data.split('&');
  var name, x, y, rot, thrust, color;
  pairs.forEach(function(e)
  {
    var parts = e.split(": ");
    var key = parts[0];
    var val = parts[1];
    if (key === "move")
      name = val;
    else if (key === "x")
      x = +val;
    else if (key === "y")
      y = +val;
    else if (key === "rot")
      rot = +val;
    else if (key === "thrust")
      thrust = +val;
    else if (key === "color")
      color = val;
  });
  var other = ships[name];
  if (!other)
  {
    other = new Ship(name);
    var liElement = document.getElementById(name);
    liElement.style.color = color;
  }
  
  other.x = x;
  other.y = y;
  other.rot = rot;
  other.thrust = thrust;
  other.color = color;
  ships[name] = other;
};

function onBulletRecv(data)
{
  var pairs = data.split('&');
  var name, x, y, rot;
  pairs.forEach(function(e)
  {
    var parts = e.split(": ");
    var key = parts[0];
    var val = parts[1];
    if (key === "bullet")
      name = val;
    else if (key === "x")
      x = +val;
    else if (key === "y")
      y = +val;
    else if (key === "rot")
      rot = +val;
  });
  var b = new Bullet(ships[name]);
  bullets.push(b);
}

function onJoin(data)
{
  var parts = data.split(': ');
  var newPlayerName = parts[1];
  var newP = document.createElement("li");
  newP.innerHTML = newPlayerName;
  newP.id = newPlayerName;
  var otherPlist = document.getElementById('otherP');
  otherPlist.appendChild(newP);
}



function getRandomColor() {
    var letters = '0123456789ABCDEF'.split('');
    var color = '#';
    for (var i = 0; i < 6; i++ ) {
        color += letters[Math.round(Math.random() * 15)];
    }
    return color;
}