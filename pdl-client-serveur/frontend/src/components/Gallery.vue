<script setup lang="ts">
import { ref } from 'vue'
import { api } from '@/http-api';
import { ImageType } from '@/image'
import Image from './Image.vue';
import { createToast } from 'mosha-vue-toastify';
import 'mosha-vue-toastify/dist/style.css';

const imageList = ref<ImageType[]>([]);

api.getImageList()
  .then((data) => {
    imageList.value = data;
  })
  .catch(e => {
    createToast({ title: ''+ e.message , description: 'Impossible de charger les images'} , {toastBackgroundColor : 'rgb(255,0,0)', type : 'danger', timeout : 5000, position : 'top-center', showIcon : true});
    console.log(e.message);
  });
</script>

<template>
  <div>
    <h3>Gallery</h3>
    <Image v-for="image in imageList" :id="image.id" />
  </div>
</template>

<style scoped>

</style>
