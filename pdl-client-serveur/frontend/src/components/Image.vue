<script setup lang="ts">
  import { defineProps } from 'vue';
  import { api } from '@/http-api';
  import router from "@/router";
  import { ref } from 'vue';
  import { ImageType } from '@/image'
  import { createToast } from 'mosha-vue-toastify';
  import 'mosha-vue-toastify/dist/style.css';

  const props = defineProps<{ id: number}>()

  const imageList = ref<ImageType[]>([]);
  getImageList();

  function getImageList() {
    api.getImageList().then((data) => {
      imageList.value = data;
    }).catch(e => {
      console.log(e.message);
      createToast({ title: ''+ e.message , description: 'Impossible de charger les images'} , {toastBackgroundColor : 'rgb(255,0,0)', type : 'danger', timeout : 5000, position : 'top-center', showIcon : true});

    });
  }
  api.getImage(props.id)
    .then((data: Blob) => {
      const reader = new window.FileReader();
      reader.readAsDataURL(data);
      reader.onload = () => {
        const galleryElt = document.getElementById("gallery-"+props.id);
        if (galleryElt !== null) {
          const imgElt = document.createElement("img");
          imgElt.id = "gal";
          galleryElt.appendChild(imgElt);
          if (imgElt !== null && reader.result as string) {
            imgElt.setAttribute("src", (reader.result as string));
          }
        }
      };
    })
    .catch(e => {
      console.log(e.message);
      createToast({ title: ''+ e.message , description: 'Impossible de charger les images'} , {toastBackgroundColor : 'rgb(255,0,0)', type : 'danger', timeout : 5000, position : 'top-center', showIcon : true});

    });


</script>

<template>

  <figure :id="'gallery-'+id"></figure>  
</template>

<style>

  #gal{
    width: 500px;
    transition: 0.2s;
    margin: 20px;

  }

  #gal:hover{
    transition: 0.2s;
    width: 600px;
    border-radius: 20px;
  }

  @media (max-width:800px) {
    #gal{
      width: 80%;
    }
  }
</style>