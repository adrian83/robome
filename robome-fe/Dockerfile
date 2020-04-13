FROM node:13.12.0-stretch-slim

ADD . /robome-fe
WORKDIR /robome-fe

RUN rm -rf node_modules

RUN npm --version
RUN npm install 

# RUN npm install react-scripts@latest

CMD ["npm", "run", "start"]