# language: ru

@smoke
@bookings
Функционал: booking-service

  @t01
  Сценарий: [booking-service] GET /bookings
    * выполнен 'GET' запрос по 'http://localhost:4040/bookings' и параметрами из таблицы и сохранен по ключу '13'
      | PARAMETER | id | 123 |
    * ответ '13' имеет статус-код '200'

  @t02
  Сценарий: [booking-service] GET /bookings/{id}
    Дано по ключу 'id' сохраняется значение 'ccd9763f-1195-4b48-87e1-125f5a364443' с типом 'string'
    * выполнен 'GET' запрос к URL 'http://localhost:4040/bookings/{id}' и параметрами из таблицы и сохранен по ключу '13'
    * ответ '13' имеет статус-код '200'
    * проверить в теле ответа '13' значение по JsonPath 'objects.size()==1' 'равно' 'true'
    * значение по JsonPath 'objects[0].customerName' из тела ответа '13' сохраняется по ключу 'name' с типом 'str'
    * по ключу 'id2' сохраняется значение '323e4567-e89b-12d3-a456-426614174007' с типом 'str'
    * сохранен SQL запрос с параметрами из табоицы и сохранен по ключу '3'
      | SELECT | customer_name     |
      | FROM   | bookings          |
      | WHERE  | id='{id2}'        |
    * выполнен SQL запрос '{3}' ответ сохранен по ключу 'nameFromDataBase' в формате 'str'
    * значение 'name' 'не равно' значению 'nameFromDataBase'




  @t03
  Сценарий: [booking-service] POST /bookings
    Дано создан json-файл с параметрами из таблицы и сохранить по ключу 'jsonBody'
      | json            | ApiFiles/esm/booking-service/CreateBookingDto.json  |
      | customerName    | Alex Mon                                            |
      | bookingDate     | 1981-04-12                                          |
      | reason          | "Lection"                                           |
    * выполнен 'POST' запрос по 'http://localhost:4040/bookings' и параметрами из таблицы и сохранен по ключу '13'
      | HEADER    | Content-Type    | application/json |
      | BODY      | {jsonBody}      |                  |
    * ответ '13' имеет статус-код '200'


  @t04
  Сценарий: [booking-service] GET /bookings/{id}
    Дано по ключу 'id' сохраняется значение '323e4567-e89b-12d3-a456-426614174009' с типом 'string'
    * выполнен 'GET' запрос к URL 'http://localhost:4040/bookings/{id}' и параметрами из таблицы и сохранен по ключу '13'
    * ответ '13' имеет статус-код '200'
    * проверить в теле ответа '13' значение по JsonPath 'objects.size()==1' 'равно' 'true'
    * значение по JsonPath 'objects[0].customerName' из тела ответа '13' сохраняется по ключу 'name' с типом 'str'
    * значение '{name}' 'не равно' значению 'Wrong Value'






