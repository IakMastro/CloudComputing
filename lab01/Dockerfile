FROM node:8.2.1-alpine

WORKDIR /code

COPY package.json /code/package.json

RUN npm install \
    && npm install -g nodemon@1.11.0 \
    && npm cache clean --force

COPY app.js /code
COPY index.html /code

CMD ["npm", "start"]