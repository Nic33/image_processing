# Software development project

This project was developed by Nicolas Dubuisson and Aurélien Gauthier. 

It is a project realized in collaboration with the University of Bordeaux.

This project is available : 

> https://imageprocessing.fr/

# Information : 

System used : MacOS, Ubuntu
Web browser : Firefox (v 110.0), Safari (v 16.3), Chrome (v 110.0)

You have two choices before starting the project. You must open a new terminal and go to :

```cd l1a/pdl-client-serveur/```

Then, you need to clean your work before :

```mvn clean install```

Finally, you can put this command:

```mvn --projects backend spring-boot:run``` 

> http://localhost:8080/

You can also open the terminal and put the following commands :

```cd l1a/pdl-client-serveur/backend/```

```mvn spring-boot:run```

And : 

```cd l1a/pdl-client-serveur/frontend/```

```npx vite --port 8080```

> http://localhost:8080/

# About the project

<ins>Languages used :</ins>

<div>

<img src="https://github.com/devicons/devicon/blob/master/icons/nodejs/nodejs-original.svg" title="NodeJS" alt="NodeJS" width="40" height="40"/>&nbsp;

<img src="https://github.com/devicons/devicon/blob/master/icons/spring/spring-original.svg" title="Spring" alt="Spring" width="40" height="40"/>&nbsp;

<img src="https://github.com/devicons/devicon/blob/master/icons/vuejs/vuejs-original.svg" title="VueJS" alt="VueJS" width="40" height="40"/>&nbsp;

<img src="https://github.com/devicons/devicon/blob/master/icons/typescript/typescript-original.svg" title="TypeScript" alt="TypeScript" width="40" height="40"/>&nbsp;

</div>

<ins>Maj :</ins>

Version 1.0 : 

Implementation of a spring boot server with vue.js.

We have created a home menu, an image gallery, a section to upload and download an image and a section to modify an image.

In the "modify" section, the user has access to 5 functions to edit his image.

Version 2.0 :

The website is compatible with mobile devices.

We changed the navigation bar and we add a footer.

We add the grey function on the "modify" section.

We created 5 new sections: 

    Editor: The purpose is to overlay two images 
    Paint : You can draw as you wish 
    Cut : You can cut a part of the image
    Bottom : If you want to remove a button on your face
    Color : To save a chosen color

PS : For each section, you can save the image on the server

Version ... :

...

# How to host this project :

We decide to host the project on [DigitalOcean](https://www.digitalocean.com/).

We followed theses links : 

https://www.youtube.com/watch?v=iGUNEnFZOgE&ab_channel=StudyGyaan

https://studygyaan.com/spring-boot/deploy-spring-boot-app-on-vm-using-nginx-https-domain

https://github.com/studygyaan/spring-boot-starter
