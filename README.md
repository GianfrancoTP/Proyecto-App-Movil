# Proyecto-App-Movil

### Integrantes:
  - Richard Le May
  - Gianfranco Traverso
  - Katherine Jara

# Instrucciones
### Vista Inicial:
- En esta vista no es necesario hacer nada, ya que sólo estaremos buscando el JSON con el usuario entregado por el usuario. Pero igualmente se puede modificar el correo y la clave, pero estas no modificaran nada del usuario, y se entrará con el correo entregado, por lo que es un log in dummy.
- Luego para poder acceder, se debe hacer click en el botón "ingresar".

### Vista De las Listas:
- En esta vista, se podra ver un header, un body y un footer.
  - El header se compone de dos secciones, en el lado izquierdo nuestro logo, y en nuestro lado derecho el usuario logeado, el cual puede ser clickeado para poder acceder a los detalles del usuario.

  - En el body será donde se van creando nuestras listas, en donde cuando tengamos una lista, tiene 3 posibilidades:
    - Reordenarla con respecto a las otras listas: Para esto es necesario mantener apretado por un rato la lista y luego la podremos arrastrar a la posición deseada.
    - Seleccionarla: Al hacerle click a esta, nos llevara a los items contenidos en esta lista.
    - Borrarla: Al lado derecho de la lista nos aparecerá un icono con un basurero, el cual es para poder eliminar la lista, con todos sus elementos.
    
  - En el footer, se contará con un boton con el signo "+", el cual sirve para poder crear las nuevas listas.

### Vista del perfil del usuario:
- En esta vista, podremos ver la foto de perfil del usuario junto con su mail más abajo (Estos datos no se pueden modificar), y contaremos con dos botones, uno para poder ver el perfil y tener la opción de editarlo, o la opción de cerrar sesión.
  - Al apretar el botón "Mi Perfil", nos apraecera un pop up mostrandonos todos los datos del usuario, y cada elemento modificable, cuenta con un lapiz a su derecha, ya que si hacemos click en un elemento no sucederá nada, por lo que para poder editar algo, será necesario hacer un click antes en el lapiz, dandonos la posibilidad de editarlo. Esto se puede ver si el text esta mas negro (tipo text) es porque no se puede modificar y si esta mas transparente (tipo hint) significa que es posible editarlo.
    - Una vez terminada la modificación del usuario, podremos presionar confirmar, y se guardarán los datos.
    - Si no se ha precionado aceptar no se guardaran los cambios.
    - Si rotamos la pantalla y aun no han presionamos confirmar, dejamos que los datos queden como estan realmente, ya que el usuario no ha guardado los datos.
  - Al precionar el botón cerrar sesión volveremos a la pantalla inicial, pero ahora contaremos con los datos modificados, si se llegaron a modificar.

### Vista de los Items de una lista:
- En esta vista también contamos con un header, un footer y un body:
  - El header cuenta con 3 secciones:
    - En el lado izquierdo tenemos un botón para poder volver a la vista de las listas.
    - En la parte central se mostrará el nombre de la lista que estamos dentro.
    - En el lado derecho contamos con un botón, siendo un lapiz, el cual permite modificar el nombre de la lista.

  - En el body también contamos con 3 secciones:
    - Parte superior: Contamos con un botón "+ Añadir to do", el cual nos permite crear nuestros items, dandonos la posibilidad de incluir todos sus atributos, como por ejemplo, si es prioritario (Mostrandose de otro color si es así), la fecha de plazo que queremos, su nombre y una descripción. La idea es que siempre se coloque nombre al item (en donde le colocamos un *, para mostrar que es necesario), pero si no lo colocan funcionará de todas formas.
    - Parte del medio, aqui se cuenta con todos los items creados en esta lista, con la opción de marcar si ya realizamos este, modificar el orden de estos, de la misma manera que se hace con las listas (mantenerlos clickeados y luego arrastrarlos para donde queramos) y tenemos la posibilidad de clickear el icono del ojo para poder entrar a los detalles de este item, y poder modificarlos si lo deseamos.
    - En la parte inferior, contamos con un switch para poder ver los items realizados o los items aun no realizados, teniendo la posibilidad de modificarlos, si nos dimos cuenta que aun no lo terminamos o si lo terminamos.

  - En el footer contamos con un botón que todavía no realiza nada, pero es para poder compartir esta lista.

### Vista de un item de una Lista:
 - En esta vista se nos muestra todos los datos del item, en donde podemos modificarlos si lo deseamos, también contamos con un header, body y footer:
  - Header: Igual que la vista de los items, contamos con una opción para volver a la lista pasada, también con el nombre del item que estamos inspeccionando y la posibilidad de modificar su nombre con el botón de un lápiz.

  - Body: Contamos con todos sus atributos, pero lo único que no puede modificarse es la fecha de creación de este item (claramente).

  - Footer: cuenta con dos botones, el cual es para eliminar el item y nos llevará a la vista pasada (vista de los items), y un botón para marcar como completado el item, o volverlo a no completado.
