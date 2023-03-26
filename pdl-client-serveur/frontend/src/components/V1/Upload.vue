<script setup lang="ts">
import { ref } from 'vue';
import { api } from '@/http-api';
import { createToast } from 'mosha-vue-toastify';
import 'mosha-vue-toastify/dist/style.css';

const target = ref<HTMLInputElement>();

function submitFile() {
  if (target.value !== null && target.value !== undefined && target.value.files !== null) {
    const file = target.value.files[0];
    if (file === undefined)
      return;
    let formData = new FormData();
    formData.append("file", file);
    api.createImage(formData).then(() => {
      if (target.value !== undefined)
        target.value.value = '';
    }).catch(e => {
      console.log(e.message);
      createToast({ title: ''+ e.message , description: 'Votre image doit être sous format PNG ou JPEG'} , {toastBackgroundColor : 'rgb(255,0,0)', type : 'danger', timeout : 5000, position : 'top-center', showIcon : true});
    });
  }
}

function handleFileUpload(event: Event) {
  target.value = (event.target as HTMLInputElement);
}

</script>

<template>
  <div>
    <h3>Upload an image</h3>
    <div>
      <input type="file" id="file" ref="file" @change="handleFileUpload" />
    </div>
    <div>
      <button @click="submitFile">Submit</button>
    </div>
  </div>
</template>

<style scoped>
</style>
