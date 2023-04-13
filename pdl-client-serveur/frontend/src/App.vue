<script setup lang="ts">

  import { createToast } from 'mosha-vue-toastify';
  import 'mosha-vue-toastify/dist/style.css';

  function handleClick(){

    var menu = document.getElementById("symbol");

    var nav = document.getElementById("nav");

    if (menu != null && nav != null){
      if (menu.innerText == "close"){
        menu.innerText = "menu";
        nav.className = "nav-menu";
      }else{
        menu.innerText = "close";
        nav.className = "nav-menu active";

      }
    }else{
      console.log("error : function handleClick()");
      createToast({ title: 'error', description: 'Une erreur est survenue'} , {toastBackgroundColor : 'rgb(255, 0, 0)', type : 'danger', timeout : 5000, position : 'top-center', showIcon : true});

    }
  }

  function closeMobileMenu(){

    var nav = document.getElementById("nav");

    if (nav != null){
      if (nav.className ==  "nav-menu active"){
        nav.className = "nav-menu";
      }else{
        nav.className = "nav-menu active";
      }
    }else{
      
      console.log("error : function closeMobileMenu()");
      createToast({ title: 'error', description: 'Une erreur est survenue'} , {toastBackgroundColor : 'rgb(255, 0, 0)', type : 'danger', timeout : 5000, position : 'top-center', showIcon : true});

    }

    handleClick();
  }

  //---------------------------------//

  function isMobileDevice() {
    return (typeof window.orientation !== "undefined") || (navigator.userAgent.indexOf('IEMobile') !== -1);
  }

  var cpt = false;

  function displayWindowSize(){

    if (cpt == false){
      
      if (isMobileDevice()) {

        cpt = true;

        createToast({ title: 'error', description: "Attention, vous utilisez le site sur votre smartphone, vous n'aurez pas accès à toutes les fonctionnalités que peut proposer le site web."} , {toastBackgroundColor : 'rgb(255, 0, 0)', type : 'danger', timeout : 10000, position : 'top-center', showIcon : true});
        setTimeout(function() {
          cpt = false;
        }, 10000);

      } else {

        const width = window.innerWidth;

        if(width < 960){

          cpt = true;

          createToast({ title: 'error', description: "Attention, vous utilisez le site sur une petite fenêtre. Pour des raisons d'ergonomies, nous désactivons les fonctionnalités 'Editor', 'Cut' et 'Bottom'."} , {toastBackgroundColor : 'rgb(255, 0, 0)', type : 'danger', timeout : 10000, position : 'top-center', showIcon : true});
          setTimeout(function() {
            cpt = false;
          }, 10000);

        }

      }
    }

  }

  window.addEventListener("resize", displayWindowSize);

  displayWindowSize();

</script>

<template>

  <link rel="stylesheet" href="https://fonts.googleapis.com/css2?family=Material+Symbols+Outlined:opsz,wght,FILL,GRAD@20..48,100..700,0..1,-50..200" />
  
  <div id="app">

    <div class="navbar"> 
      
      <div class="menu-bar" @click="handleClick()">
        <span id = "symbol" class = "material-symbols-outlined">
          menu
        </span>
      </div>

      <a class ="navbar-logo" >
        <router-link to="/" >
          <span class="material-symbols-outlined">
            home
          </span>
        </router-link>
      </a>

      <ul id = "nav" class="nav-menu">

        <a class="vl2">
          <div></div> 
        </a>

        <a @click="closeMobileMenu()">
          <router-link to="/gallery">
            <span class="material-symbols-outlined">
              photo_library
            </span>
          </router-link>
        </a>

        <a @click="closeMobileMenu()">
          <router-link to="/upload">
            <span class="material-symbols-outlined">
              upload
            </span>
          </router-link>
        </a>

        <a @click="closeMobileMenu()">
          <router-link to="/download">
            <span class="material-symbols-outlined">
              download
            </span>
          </router-link>
        </a>

        <a @click="closeMobileMenu()">
          <router-link to="/modify">
          <span class="material-symbols-outlined">
            magic_exchange
          </span>
        </router-link>
        </a>

        <a class="vl2">
          <div></div> 
        </a>

        <a id = "only_PC" @click="closeMobileMenu()">
          <router-link to="/editor">
            <span class="material-symbols-outlined">
              photo_size_select_small
            </span>
          </router-link>
        </a>

        <a @click="closeMobileMenu()">
          <router-link to="/paint">
          <span class="material-symbols-outlined">
            palette
          </span>   
          </router-link>
        </a>

        <a id = "only_PC" @click="closeMobileMenu()">
          <router-link to="/contour">
          <span class="material-symbols-outlined">
            cut
          </span>
        </router-link>
        </a>

        <a id = "only_PC" @click="closeMobileMenu()">
          <router-link to="/bottom">
            <span class="material-symbols-outlined">
              align_flex_end
            </span>
          </router-link>
        </a>

        <a id = "only_PC" @click="closeMobileMenu()">
          <router-link to="/color">
            <span class="material-symbols-outlined">
              format_color_reset
            </span>
          </router-link>
        </a>

      </ul>

    </div>

    <div>
      <h1>Image processing</h1>
      <router-view />
    </div>

    <footer>
      <h2>Important</h2>

      <p>Ce site web a été développé par 
        <a target="_blank" href="https://www.linkedin.com/in/aurelien-gauthier/">Aurélien Gauthier</a>
        et 
        <a target="_blank" href="https://www.linkedin.com/in/nicolas-dubuisson-b4b856226/">Nicolas Dubuisson</a>.
      </p>
      <p>Ce projet s'inscrit dans le cadre de notre matière Projet Développement Logiciel de la Licence Informatique de l'Université de Bordeaux.</p>
      <p> Nous n'avons aucun objectif commercial, nous voulons juste montrer nos connaissances en termes de développement informatique.</p>
      
      <br>

      <h2>Technologies utilisées</h2>
      <a>Java</a>
      <br>
      <br>
      <a target="_blank" href="https://spring.io/">Spring Boot</a>
      <br>
      <br>
      <a target="_blank" href="https://vuejs.org/guide/typescript/overview.html">Vue.js (TypeScript)</a>

      <br>
      <br>

      <h2>Remerciment</h2>
      <a target="_blank" href="https://www.u-bordeaux.fr/">
          <img class="logo" src="./assets/Universite_Bordeaux_RVB-08.png"> 
      </a>

      <br>
      <br>

      <h2>Confidentialité</h2>

      <a rel="license" href="http://creativecommons.org/licenses/by-nc-nd/4.0/">
        <img alt="Licence Creative Commons" style="border-width:0" src="https://i.creativecommons.org/l/by-nc-nd/4.0/88x31.png" />
      </a>
      <br/>Ce(tte) œuvre est mise à disposition selon les termes de la 
      <a rel="license" href="http://creativecommons.org/licenses/by-nc-nd/4.0/">
        Licence Creative Commons Attribution - Pas d&#39;Utilisation Commerciale - Pas de Modification 4.0 International
      </a>.
    </footer>
  </div>
</template>

<style>

#app {
  text-align: center;
  font-family: Arial, Helvetica, sans-serif;
  color: #2c3e50;
}

.navbar {
  background-color:  rgb(0, 0, 0);
  height: 70px;
  border-radius: 10px;
  display: flex;
  text-align: center;
  align-items: center;

  position: relative;

  padding-left: 0;
}

.menu-bar {
  display: none;
}

.nav-menu {
  position: absolute;
  display: inline;
  left: 0px;
}

.navbar-logo {

  display: flex;
  text-align: center;
  align-items: center;

  text-decoration: none;
  font-size: 1.5rem;

  margin-left: 20px ;
  z-index:1;
}

.nav-menu > a{
  padding-left: 30px;
  
}

.vl2 > div {

  position: absolute;
  display: inline;
  border-left: 6px solid green;
  height: 70px;
  text-decoration: none;

  transform: translate(-100%, -30%);

}


@media screen and (max-width: 960px) {

  .vl2{
    display: none;
  }

  .nav-menu {

    display: flex;
    flex-direction: column;

    top: 60px;
    left: -100%;
    opacity: 1;
    
    transition: all 0.5s ease;

    border-radius: 10px;

  }

  .nav-menu.active {

    background-color:  green;
    left: 0;
    opacity: 1;
    transition: all 0.5s ease;
    z-index: 1;
  }

  .nav-menu > a {

    padding-bottom: 10px;
    padding-top: 10px;
    padding-left: 0px;
    padding-right: 40px;
    
  }

  .menu-bar {

    display: block;
    position: absolute;
    top: 22px;
    right: 20px;
    color:rgb(255, 255, 255);

  }

  #only_PC{
    display:none;
  }

}

.material-symbols-outlined {

  font-variation-settings:
  'FILL' 0,
  'wght' 700,
  'GRAD' 0,
  'opsz' 48
}

footer{
  background-color: black;
  color:white;

  border-radius: 10px;
  padding: 20px;
  margin-top: 20px;

}

.logo{
  width: 250px;
  border-radius: 10px;
}

h2{
  color:green;
}

a{
  color : white;
  text-decoration: none;
}

</style>
