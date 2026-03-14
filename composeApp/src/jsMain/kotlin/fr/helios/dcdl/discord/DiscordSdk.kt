package fr.helios.dcdl.discord

fun setupDiscordSdk() {
    js("""(function() {

console.log("discordSdk.js - importing SDK");
var DiscordSDK = require("@discord/embedded-app-sdk").DiscordSDK;
console.log("discordSdk.js - SDK importé");

window.__discordHost = window.location.host

var CLIENT_ID = process.env.DISCORD_CLIENT_ID;
var discordSdk = new DiscordSDK(CLIENT_ID);
var auth = undefined

function setupDiscord() {
    discordSdk.ready().then(function() {
        console.log("SDK ready");

        return discordSdk.commands.authorize({
            client_id: CLIENT_ID,
            response_type: "code",
            state: "",
            prompt: "none",
            scope: ["identify", "guilds"]
        });
    }).then(function(result) {
        console.log("authorize OK");

        return fetch("/api/discord/token", {
            method: "POST",
            body: JSON.stringify({ code: result.code }),
            headers: { "Content-Type": "application/json" }
        }).then(function(r) { return r.json(); });
    }).then(function(tokenData) {
        console.log("token OK");

        return discordSdk.commands.authenticate({
            access_token: tokenData.access_token
        });
    }).then(function(authResponse) {
        console.log("Authentifié !");
        auth = authResponse

        return getVoiceChannel();
    }).then(function(channel) {
        window.__activityChannelId = channel.id;

        return getUser()
    }).then(function(user) {
        window.__user = {
            id: user.id,
            username: user.username
        };

        window.__discordReady = true;
        window.dispatchEvent(new Event("discordReady"));
    }).catch(function(err) {
        console.error("Erreur Discord:", err);
    });
}

function getVoiceChannel() {
  if (discordSdk.channelId != null && discordSdk.guildId != null) {
    return discordSdk.commands.getChannel({channel_id: discordSdk.channelId}).then(function(channel) {
      if (channel.id != null) {
        return channel
      }
    });
  }
}

function getUser() {
  return fetch("https://discord.com/api/v10/users/@me", {
    headers: {
      Authorization: 'Bearer ' + auth.access_token,
      'Content-Type': 'application/json'
    }
  }).then(function(r) { return r.json() });
}

setupDiscord();

        })();""")
}
